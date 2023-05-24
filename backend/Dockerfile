FROM node:latest

WORKDIR /srv/app

COPY package*.json ./

RUN npm install

COPY . .

RUN npm run build

ENV PORT=3000

EXPOSE 2999

CMD ["node", "dist/server.js"]
