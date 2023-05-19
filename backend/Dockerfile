FROM node:latest

WORKDIR /srv/app

COPY package*.json ./

RUN npm install

COPY . .

RUN npm run build

ENV PORT=3000

EXPOSE 3000

CMD ["node", "dist/server.js"]