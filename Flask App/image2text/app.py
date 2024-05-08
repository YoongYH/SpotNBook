from flask import Flask, Blueprint, request, jsonify
import requests
from PIL import Image
import pytesseract
import re

image_recognition_bp = Blueprint('image_recognition_bp', __name__)

# Route to handle image URL and text extraction
@image_recognition_bp.route('/extract_text', methods=['POST'])
def extract_text():
  if 'image_url' not in request.json:
    return jsonify({'error': 'No image URL provided'})

  # Get the image URL from the request
  image_url = request.json['image_url']

  # Download the image from the URL
  try:
    response = requests.get(image_url, stream=True)
    response.raise_for_status()  # Raise an error for bad status codes
  except requests.exceptions.RequestException as e:
    return jsonify({'error': f'Failed to download image: {e}'})

  # Open the image from the response
  with open('temp_image.jpg', 'wb') as f:
    for chunk in response.iter_content(1024):
      f.write(chunk)
  image = Image.open('temp_image.jpg')

  # Use Tesseract OCR to extract text
  extracted_text = pytesseract.image_to_string(image)

  # Extract merchant name and registration number from the extracted text
  merchant_name, registration_no = extract_info_from_text(extracted_text)

  # Remove temporary image file
  import os
  os.remove('temp_image.jpg')

  return jsonify({'merchant_name': merchant_name, 'registration_no': registration_no})

def extract_info_from_text(text):
    pattern = r"This is to certify that\s*(.*)"
    match = re.search(pattern, text, re.DOTALL)

    if match:
        matched_text = match.group(1)
        lines = matched_text.split('\n')

        count = 0
        for line in lines:
            if line.strip():
                if count == 0:
                    business_name = line.strip()
                elif count == 1:
                    registration_no = line.strip()
                count += 1
            if count == 2:
                break
              
        return business_name, registration_no
    else:
        return None, None