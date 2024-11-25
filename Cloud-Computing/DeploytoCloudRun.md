
# **Deploy to Google Cloud Run with CI/CD**

This document describes how to deploy a **Node.js backend API** to **Google Cloud Run** using **GitHub Actions** for a seamless CI/CD workflow.

---

## **1. Prerequisites**
1. **Google Cloud Project** with active billing.
2. The following APIs enabled in your Google Cloud project:
   - **Cloud Run API**
   - **Container Registry API**
3. A **service account** with the following roles:
   - Cloud Run Admin
   - Storage Admin
   - Service Account User
4. A GitHub repository containing your backend code.
5. Secrets added to your GitHub repository:
   - `GCLOUD_SERVICE_KEY`: Your service account key in JSON format.
   - `GCLOUD_PROJECT_ID`: Your Google Cloud Project ID.
   - `GCLOUD_REGION`: The region where your service will be deployed.
   - Other environment variables required by your app.

---

## **2. Workflow Configuration**

Below is the GitHub Actions workflow configuration (`.github/workflows/deploy.yml`):

```yaml
name: Deploy to Cloud Run

on:
    push:
        paths:
            - "Cloud-Computing/backend-api/**"
    workflow_dispatch:

jobs:
    deploy:
        runs-on: ubuntu-latest
        permissions:
            contents: "read"
            id-token: "write"

        steps:
            - name: Checkout Code
              uses: actions/checkout@v3

            - name: Google Auth
              uses: "google-github-actions/auth@v1"
              with:
                  credentials_json: "${{ secrets.GCLOUD_SERVICE_KEY }}"

            - name: Set up Cloud SDK
              uses: "google-github-actions/setup-gcloud@v1"

            - name: Authenticate Docker with GCP
              run: gcloud auth configure-docker

            - name: Build and Push Docker Image
              run: |
                  IMAGE_NAME="gcr.io/${{ secrets.GCLOUD_PROJECT_ID}}/cc-service"
                  cd Cloud-Computing/backend-api
                  docker build -t $IMAGE_NAME .
                  docker push $IMAGE_NAME

            - name: Deploy to Cloud Run
              run: |
                  gcloud run deploy cc-service \
                    --image=gcr.io/${{ secrets.GCLOUD_PROJECT_ID}}/cc-service \
                    --region=${{ secrets.GCLOUD_REGION }} \
                    --platform=managed \
                    --allow-unauthenticated \
                    --set-env-vars GOOGLE_CLIENT_ID=${{ secrets.GOOGLE_CLIENT_ID }},GOOGLE_CLIENT_SECRET=${{ secrets.GOOGLE_CLIENT_SECRET }},REDIRECT_URI=${{ secrets.REDIRECT_URI }},GEMINI_API_KEY=${{ secrets.GEMINI_API_KEY }},BUCKET_NAME=${{ secrets.BUCKET_NAME }},DB_USERNAME=${{ secrets.DB_USERNAME }},DB_PASSWORD=${{ secrets.DB_PASSWORD }},DB_DATABASE=${{ secrets.DB_DATABASE }},DB_HOST=${{ secrets.DB_HOST }},DB_DIALECT=${{ secrets.DB_DIALECT }},BUCKET_KEY=${{ secrets.BUCKET_KEY }}
```

---

## **3. Dockerfile**

Below is the `Dockerfile` used to build your Node.js backend API:

```Dockerfile
# Use the required Node.js version
FROM node:22

# Set working directory
WORKDIR /app

# Copy required files
COPY package*.json ./
COPY . .

# Install dependencies
RUN npm install

# Expose the port the app runs on
EXPOSE 3000

# Start the application
CMD ["npm", "start"]
```

---

## **4. How It Works**

1. **Trigger:** The workflow runs when there are changes in the `Cloud-Computing/backend-api/` directory or manually triggered using `workflow_dispatch`.
2. **Authentication:** GitHub Actions authenticates with Google Cloud using the provided service account key.
3. **Docker Image:** The backend code is built into a Docker image and pushed to Google Container Registry.
4. **Deployment:** The built image is deployed to Google Cloud Run with the specified environment variables.

---

## **5. Useful Commands**

### Deploy Locally
If you need to deploy manually, you can use the following commands:
```bash
gcloud auth login
gcloud run deploy cc-service \
  --image=gcr.io/<PROJECT_ID>/cc-service \
  --region=<REGION> \
  --platform=managed \
  --allow-unauthenticated
```

---

## **6. Notes**

- Make sure all secrets are correctly configured in GitHub.
- Update the `Dockerfile` and environment variables as per your application's requirements.
- Verify that the necessary APIs are enabled in your Google Cloud project.
