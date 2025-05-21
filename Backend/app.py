from flask import Flask, request, jsonify
import tensorflow as tf
import numpy as np
import cv2
import json
import os
import logging
import threading
import time
from pyngrok import ngrok, conf

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))


MODEL_INFO_FILENAME = 'pawshield_model_info.json'
TREATMENT_FILENAME = 'Treatment.json'
DOG_MODEL_FILENAME = "Dog_ResNet50V2_Transfer_tf_saved_model.keras"
CAT_MODEL_FILENAME = "Cat_MobileNetV2_Transfer_tf_saved_model.keras"

MODEL_INFO_PATH = os.path.join(SCRIPT_DIR, MODEL_INFO_FILENAME)
TREATMENT_JSON_PATH = os.path.join(SCRIPT_DIR, TREATMENT_FILENAME)
dog_model_path = os.path.join(SCRIPT_DIR, DOG_MODEL_FILENAME)
cat_model_path = os.path.join(SCRIPT_DIR, CAT_MODEL_FILENAME)


#Auth token to connect to ngrok
NGROK_AUTH_TOKEN = "2v2CooAbsU99Q9h4oSCJ2mE8L11_6FG4CiJCkR9fMDGHeEyoV"

app = Flask(__name__)
logging.basicConfig(level=logging.INFO)

model_info = None
dog_class_names = None
cat_class_names = None
IMG_SIZE = None
MEAN_NORM = None
STD_NORM = None
dog_model = None
cat_model = None
treatment_data = None

with open(MODEL_INFO_PATH, 'r') as f:
    model_info = json.load(f)

dog_class_names = model_info.get('dog_classes')
cat_class_names = model_info.get('cat_classes')

img_size_list = model_info.get('img_size')
if img_size_list and len(img_size_list) == 2:
    IMG_SIZE = tuple(img_size_list)

mean_norm_list = model_info.get('mean_norm')
std_norm_list = model_info.get('std_norm')
if mean_norm_list and std_norm_list:
    MEAN_NORM = np.array(mean_norm_list, dtype=np.float32)
    STD_NORM = np.array(std_norm_list, dtype=np.float32)

model_configs = model_info.get('model_configs', {})
dog_config = model_configs.get('Dog', {})
cat_config = model_configs.get('Cat', {})

dog_backbone = dog_config.get('backbone', 'Unknown')
dog_strategy = dog_config.get('strategy', 'Unknown').capitalize()
cat_backbone = cat_config.get('backbone', 'Unknown')
cat_strategy = cat_config.get('strategy', 'Unknown').capitalize()

#Check if there is any model available to load
if model_info:
    if os.path.exists(dog_model_path):
        dog_model = tf.keras.models.load_model(dog_model_path)
    
    if os.path.exists(cat_model_path):
        cat_model = tf.keras.models.load_model(cat_model_path)

with open(TREATMENT_JSON_PATH, 'r') as f:
    treatment_data = json.load(f)

# Incase there is a mismatch with the treatment data a default message is set
def default_treatment_message():
    return {
        "description": "Treatment information not available for this specific condition.",
        "symptoms": ["Consult a veterinarian for accurate symptoms."],
        "treatments": ["Please consult with a veterinarian for diagnosis and treatment options."],
        "veterinary_visit": "Recommended"
    }

def get_treatment_recommendation(animal_type, disease_name, current_treatment_data):
    animal_key = animal_type.lower()
    disease_key = disease_name.lower().replace(" ", "_").replace("-", "_")

    if not isinstance(current_treatment_data, dict) or not current_treatment_data:
        return default_treatment_message()

    animal_treatments = current_treatment_data.get(animal_key)
    if animal_treatments is None:
        return default_treatment_message()

    if not isinstance(animal_treatments, dict):
        return default_treatment_message()

    specific_treatment = animal_treatments.get(disease_key)
    if specific_treatment is None:
        return default_treatment_message()

    complete_treatment = default_treatment_message()
    complete_treatment.update(specific_treatment)
    return complete_treatment

#Main code for predicting the disease and giving the treatment recommendation
@app.route('/predict', methods=['POST'])
def predict():
    if model_info is None or IMG_SIZE is None or MEAN_NORM is None or STD_NORM is None \
       or dog_class_names is None or cat_class_names is None:
         return jsonify({'error': 'Server configuration error: Essential info missing'}), 500

    if 'image' not in request.files:
        return jsonify({'error': 'Missing "image" part in form-data'}), 400
    file = request.files['image']
    if file.filename == '':
        return jsonify({'error': 'No image selected'}), 400

    animal_type = request.form.get('animal_type', '').lower()
    if animal_type not in ['dog', 'cat']:
        return jsonify({'error': 'Missing or invalid "animal_type" (must be "dog" or "cat")'}), 400

    selected_model = None
    class_names = None
    if animal_type == 'dog':
        selected_model = dog_model
        class_names = dog_class_names
        if selected_model is None:
             return jsonify({'error': 'Server error: Disease model for Dog is currently unavailable'}), 503
    elif animal_type == 'cat':
        selected_model = cat_model
        class_names = cat_class_names
        if selected_model is None:
             return jsonify({'error': 'Server error: Disease model for Cat is currently unavailable'}), 503

    #Converts the image to a readable file for the model
    img_bytes = file.read()
    img_np = cv2.imdecode(np.frombuffer(img_bytes, np.uint8), cv2.IMREAD_COLOR)
    if img_np is None: return jsonify({'error': 'Could not decode image'}), 400
    img_rgb = cv2.cvtColor(img_np, cv2.COLOR_BGR2RGB)
    img_resized = cv2.resize(img_rgb, IMG_SIZE, interpolation=cv2.INTER_NEAREST)
    img_float = img_resized.astype(np.float32)
    img_rescaled = img_float / 255.0
    img_normalized = (img_rescaled - MEAN_NORM) / STD_NORM
    img_expanded = np.expand_dims(img_normalized, axis=0)

    prediction = selected_model.predict(img_expanded, verbose=0)
    probabilities = tf.nn.softmax(prediction[0]).numpy()
    predicted_class_index = np.argmax(probabilities)

    if predicted_class_index >= len(class_names):
        return jsonify({'error': 'Internal server error: Model prediction mismatch'}), 500

    predicted_class_name = class_names[predicted_class_index]

    treatment_info = get_treatment_recommendation(animal_type, predicted_class_name, treatment_data)

    #The outcome to send to the frontend
    result = {
        'animal_type': animal_type,
        'diagnosis': predicted_class_name,
        'probabilities': [round(float(p), 4) for p in probabilities],
        'treatment_info': treatment_info
    }
    return jsonify(result)

@app.route('/')
def home():
    status = "OK"
    issues = []
    if model_info is None: issues.append("Model Info JSON not loaded")
    if dog_model is None: issues.append("Dog Model not loaded")
    if cat_model is None: issues.append("Cat Model not loaded")
    if treatment_data is None or not treatment_data: issues.append("Treatment Data not loaded or empty")
    if IMG_SIZE is None or MEAN_NORM is None or STD_NORM is None: issues.append("Image constants not loaded")

    if issues:
        status = f"WARNING: Issues detected - {', '.join(issues)}"
    return f"PawShield API (Local w/ Ngrok) is running! Status: {status}. Use /predict endpoint."

def run_flask_app():
    app.run(host='0.0.0.0', port=5000, debug=False, use_reloader=False)

# Run the Flask app in a separate thread
if __name__ == '__main__':
    if NGROK_AUTH_TOKEN:
        conf.get_default().auth_token = NGROK_AUTH_TOKEN
    
    flask_thread = threading.Thread(target=run_flask_app)
    flask_thread.daemon = True
    flask_thread.start()
    
    time.sleep(10)
    
    public_url = None
    try:
        public_url = ngrok.connect(addr=5000, proto="http", hostname="pawshield.ngrok.app")
        
        while True:
            time.sleep(60)
    finally:
        if public_url:
            ngrok.disconnect(public_url)
