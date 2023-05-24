FROM node:18.16.0 as build
WORKDIR /app
COPY . .
RUN npm install
RUN npm run build

FROM nginx:alpine
COPY ./nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/secret_santa /usr/share/nginx/html
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
