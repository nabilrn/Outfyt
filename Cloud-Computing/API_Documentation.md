
# API Documentation

## Overview
This document outlines the API endpoints for a backend service handling user authentication, Google Calendar data, news scraping, chat with Gemini AI, and image uploads for personal color detection.

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
    "events": [ ... ],
    "newAccessToken": "string (optional)"
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
    "data": [ ... ]
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

## Chat with Gemini AI

### **POST** `api/chat/start`
Start a new chat session with Gemini AI.

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
Send a message to Gemini AI and receive a response.

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
      "promptFeedback": "string"
    }
  }
  ```

### **POST** `api/chat/stream`
Stream real-time chat responses from Gemini AI.

#### Headers:
- `Authorization`: Bearer `<AccessToken>`

#### Request Body:
```json
{
  "message": "string"
}
```

#### Responses:
- **Streamed Event Data**

---

## Image Upload for Personal Color

### **POST** `api/upload-image`
Upload an image for personal color analysis.

#### Headers:
- `Authorization`: Bearer `<AccessToken>`

#### Form Data:
- `image`: (File) The image to be analyzed.

#### Responses:
- **200 OK**
  ```json
  {
    "message": "File uploaded successfully",
    "details": { ... }
  }
  ```
- **400 Bad Request**
  ```json
  {
    "message": "No file uploaded"
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
    "message": "Server running successfully"
  }
  ```

---

## Error Handling
All endpoints respond with appropriate error codes and messages in case of failure.

