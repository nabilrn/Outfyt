FROM node:21.6.2-slim

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci --only=production

# Make sure to copy the config directory specifically
COPY config ./config
COPY models ./models
COPY controller ./controller
COPY migrations ./migrations
COPY seeders ./seeders
COPY *.js ./

# Create user node
USER node

EXPOSE 8080

CMD ["npm", "start"]