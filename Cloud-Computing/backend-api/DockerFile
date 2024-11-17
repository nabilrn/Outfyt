FROM node:21.6.2

WORKDIR /server


COPY package*.json ./
COPY . .


RUN npm install


CMD ["npm", "start"]
