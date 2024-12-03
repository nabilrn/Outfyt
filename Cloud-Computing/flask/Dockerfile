# Use the official Python image as the base image
FROM python:3.9-slim

# Set the working directory in the container
WORKDIR /app

# Copy the current directory contents into the container
COPY . /app

# Install system dependencies and required Python packages
RUN apt-get update && apt-get install -y \
    libgl1-mesa-glx \
    libglib2.0-0 \
    && rm -rf /var/lib/apt/lists/*

# Install Python dependencies
RUN pip install --no-cache-dir flask tensorflow pillow numpy requests

# Expose the port the app runs on
EXPOSE 8080

# Define the environment variable for Flask
ENV FLASK_APP=main.py

# Run the application
CMD ["flask", "run", "--host=0.0.0.0", "--port=8080"]
