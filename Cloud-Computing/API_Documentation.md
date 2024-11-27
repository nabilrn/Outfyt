# API Documentation

## Overview
This document provides comprehensive details about the backend API endpoints for a feature-rich application including user authentication, Google Calendar integration, AI chat, image upload, and personal color analysis.

---

## Authentication

### **POST** `api/auth`
Authenticate a user using Google OAuth tokens.

#### Request Body:
```json
{
  "idToken": "string",
  "authCode": "string"
}
```

#### Responses:
- **200 OK**
  ```json
  {
    "success": true,
    "user": {
      "googleId": "string",
      "displayName": "string", 
      "email": "string",
      "photoUrl": "string"
    },
    "accessToken": "string"
  }
  ```
- **401 Unauthorized**
  ```json
  {
    "success": false,
    "error": "Authentication failed"
  }
  ```

### **POST** `api/refresh-token`
Refresh an expired access token.

#### Responses:
- **200 OK**
  ```json
  {
    "accessToken": "string",
    "success": true
  }
  ```
- **401 Unauthorized**
  ```json
  {
    "success": false,
    "error": "Invalid refresh token"
  }
  ```

---

## Google Calendar

### **GET** `api/calendar`
Retrieve upcoming events from the user's primary Google Calendar.

#### Headers:
- `Authorization`: Bearer `<AccessToken>`

#### Responses:
- **200 OK**
  ```json
  {
    "success": true,
    "events": [ 
      // Array of calendar events
    ]
  }
  ```
- **500 Internal Server Error**
  ```json
  {
    "success": false,
    "error": "Failed to fetch calendar data"
  }
  ```

---

## News Scraping

### **GET** `api/news`
Fetch latest fashion-related articles.

#### Headers:
- `Authorization`: Bearer `<AccessToken>`

#### Responses:
- **200 OK**
  ```json
  {
    "status": "success",
    "count": number,
    "data": [
      {
        "title": "string",
        "synopsis": "string",
        "author": "string",
        "link": "string",
        "imageUrl": "string"
      }
    ]
  }
  ```
- **500 Internal Server Error**
  ```json
  {
    "status": "error",
    "message": "Failed to scrape the website"
  }
  ```

---

## AI Chat with Gemini

### **POST** `api/chat/start`
Initialize a new chat session.

#### Headers:
- `Authorization`: Bearer `<AccessToken>`

#### Responses:
- **200 OK**
  ```json
  {
    "status": "success",
    "message": "Chat session started successfully"
  }
  ```

### **POST** `api/chat/send`
Send a message and receive a response.

#### Headers:
- `Authorization`: Bearer `<AccessToken>`

#### Request Body:
```json
{
  "message": "string"
}
```

#### Responses:
- **200 OK**
  ```json
  {
    "status": "success",
    "data": {
      "reply": "string",
      "promptFeedback": "object"
    }
  }
  ```

### **POST** `api/chat/stream`
Stream real-time chat responses.

#### Headers:
- `Authorization`: Bearer `<AccessToken>`

#### Request Body:
```json
{
  "message": "string"
}
```

#### Responses:
- **Streamed Server-Sent Events (SSE)**
  - Chunks of response text
  - `event: end` when complete
  - `event: error` if something goes wrong

---

## Image Upload

### **POST** `api/upload-image`
Upload an image for personal color analysis.

#### Headers:
- `Authorization`: Bearer `<AccessToken>`

#### Form Data:
- `image`: (File) Image to be analyzed
- `gender`: User's gender
- `age`: User's age

#### Responses:
- **200 OK**
  ```json
  {
    "message": "Upload berhasil dan data diperbarui",
    "url": "string",
    "predicted_class": "string",
    "gender": "string",
    "age": "number",
    "genderCategory": "string"
  }
  ```
- **400 Bad Request**
  ```json
  {
    "message": "Tidak ada file yang di-upload."
  }
  ```

---

## Personal Color Analysis

### **GET** `api/personal-color`
Retrieve personal color analysis results.

#### Headers:
- `Authorization`: Bearer `<AccessToken>`

#### Responses:
- **200 OK**
  ```json
  {
    "faceImageUrl": "string",
    "colorType": "string",
    "genderCategory": "string",
    "recommendedColors": [
      "string" // List of color hex codes
    ]
  }
  ```
- **404 Not Found**
  ```json
  {
    "error": "User not found"
  }
  ```

---

## Health Check

### **GET** `api/test`
Verify the server is running.

#### Responses:
- **200 OK**
  ```json
  {
    "message": "Awesome, Server running successfully"
  }
  ```

---

## Error Handling
All endpoints respond with appropriate HTTP status codes and error messages in case of failure.

## Authentication
All endpoints (except `/api/test` and `/api/auth`) require a valid access token in the `Authorization` header.