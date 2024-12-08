# Infrastructure Overview

This document provides an overview of our cloud infrastructure, including the services and tools utilized for deployment, storage, API integration, and application management.

---

## Components Overview

### 1. **Cloud Run**
   - **Purpose**: Hosts our backend API as a serverless containerized application.
   - **Key Features**:
     - Automatically scales with traffic.
     - Integrated with Artifact Registry for Docker image deployment.
     - Deployed with environment variables for configuration.

### 2. **Cloud Storage Bucket**
   - **Purpose**: Used for storing user-uploaded files, such as documents and images.
   - **Key Features**:
     - Secure storage with fine-grained access control.
     - Handles uploads, including public/private file access.
     - Integrated with backend for seamless file management.

### 3. **OAuth Screen**
   - **Purpose**: Provides secure user authentication and authorization using Google OAuth 2.0.
   - **Key Features**:
     - User login via Google Accounts.
     - Scopes configured for accessing Calendar API securely.

### 4. **Google Calendar API**
   - **Purpose**: Fetches and manages user schedules for integration into the application.
   - **Key Features**:
     - Integrated into the backend API.
     - Used for fetching real-time schedule data.
     - Ensures secure access through OAuth.

### 5. **Gemini API**
   - **Purpose**: Provides additional functionality (e.g., external data integration or services).
   - **Key Features**:
     - API key secured through environment variables.
     - Supports seamless integration into backend workflows.

### 6. **Cloud SQL**
   - **Purpose**: A managed relational database for storing structured application data.
   - **Key Features**:
     - PostgreSQL/MySQL database instance for user and application data.
     - Connected securely to Cloud Run via private network or public IP with authorized IPs.

### 7. **Artifact Registry**
   - **Purpose**: Stores and manages Docker images for deployment to Cloud Run.
   - **Key Features**:
     - Secure and version-controlled image storage.
     - Integrated directly into the CI/CD pipeline for efficient deployment.

---

## Architecture Diagram

![Outfyt Google Cloud Architecture](https://github.com/user-attachments/assets/04c5fbf1-5953-4aae-bb79-5295f7b2d4de)

Below is a high-level overview of the architecture:

1. **User Authentication**:  
   - Users log in via the OAuth screen, granting permissions to access Google Calendar.
   
2. **Backend API (Cloud Run)**:  
   - Interacts with the frontend and other services such as Google Calendar API and Gemini API.
   - Pulls Docker images from Artifact Registry during deployments.

3. **Cloud Storage**:  
   - Handles storage and retrieval of user files.

4. **Cloud SQL**:  
   - Stores structured data, including user profiles, schedules, and application logs.

5. **Services Integration**:  
   - Google Calendar API and Gemini API support real-time application functionality.

---

## Deployment and Management

- **CI/CD**: GitHub Actions is configured to automate deployments to Cloud Run using a secure CI/CD pipeline.
- **Environment Variables**: Managed securely using Google Secret Manager and GitHub secrets.
- **Monitoring**: Google Cloud Monitoring is utilized for tracking system performance and logs.

---

## Future Improvements
- Adding load balancing with Cloud Load Balancer for frontend integration.
- Enhancing security with VPC peering for Cloud Run and Cloud SQL.

---

For any issues or questions, please contact the cloud team.
