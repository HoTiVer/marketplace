# 🛒 Marketplace Platform — Web Technologies & Databases Course Project
### 📌 General Description

This project is a full-featured marketplace web application developed as a coursework for Web Technologies and Databases.

A marketplace is a type of e-commerce platform where:

- Sellers can register and publish products

- Customers can browse categories, search items, place orders

- The platform handles product listing, carts, wishlists, messaging, and order functionality

### This application implements all core marketplace components:

✅ Product catalog

✅ Seller management and registration workflow

✅ Shopping cart & wishlist

✅ Checkout system with order tracking

✅ Buyer–Seller direct messaging

✅ Role-based access control

❌ Email notifications

❌ Google OAuth


### 🔧 Tech Stack

- Backend: Java 21, Spring Boot

- Security:	Spring Security, JWT, Google OAuth

- Database: PostgreSQL

- Cache / Token Store: Redis

- Frontend: HTML, JS Modules, TailwindCSS

- Deployment: Docker


### 🧬 Architecture Overview

Module structure

JWT Access and Refresh Tokens

Redis as high-performance storage for blacklisted tokens

PostgreSQL as main persistent storage

Spring Data JPA for ORM & query abstraction

----

### 🧑‍💻 Requirements

- Java 17+

- Docker & Docker Compose

- PostgreSQL

- Redis

- .env configuration file

### 🔐 Environment Variables

Create .env file in project root:

```
DB_URL=jdbc:postgresql://localhost:5432/mydb
DB_NAME=mydb
DB_USERNAME=postgres
DB_PASSWORD=password

JWT_SECRET=your_jwt_secret
JWT_ACCESS_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=604800000

REDIS_HOST=localhost

SERVICE_EMAIL=your_email@gmail.com
EMAIL_PASSWORD=your_app_password

GOOGLE_CLIENT_ID=google_client_id_here
GOOGLE_CLIENT_SECRET=google_client_secret_here
```
### ▶️ Running the Application

`docker-compose up --build`

App runs at:

👉 http://localhost:8080