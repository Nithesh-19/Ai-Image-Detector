# AI Image Detector

An AI-powered web application that analyzes an uploaded image and predicts whether the image is **AI-generated or human-created**.

The project uses a **React frontend** and a **Spring Boot backend**. The backend processes the uploaded image and uses an ONNX-based AI detection model to generate the prediction and confidence scores.

## Features

* Upload an image for AI-generated content detection
* AI-generated vs human-created prediction
* Confidence percentage for the prediction
* React-based user interface
* Spring Boot REST API
* ONNX model integration
* MySQL database integration for detection-related data
* Image upload handling
* Maven-based backend project

## Tech Stack

### Frontend

* React.js
* JavaScript
* HTML5
* CSS3
* Vite

### Backend

* Java
* Spring Boot
* Spring Data JPA
* Hibernate
* REST API
* Maven

### Database

* MySQL

### AI / Machine Learning

* ONNX Runtime
* ONNX model
* Image preprocessing
* AI-generated image classification

## Project Structure

```text
Ai-Image-Detector/
│
├── ai-content-detector/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   ├── pom.xml
│   └── mvnw
│
├── frontend/
│   ├── src/
│   ├── public/
│   ├── package.json
│   └── vite.config.js
│
└── README.md
```

## How It Works

The application follows this flow:

```text
User
  │
  │ Uploads image
  ▼
React Frontend
  │
  │ HTTP Request
  ▼
Spring Boot REST API
  │
  │ Image preprocessing
  ▼
ONNX Detection Model
  │
  │ Prediction
  ▼
Spring Boot Backend
  │
  │ Detection result
  ▼
React Frontend
  │
  ▼
Prediction + Confidence
```

The uploaded image is processed by the backend and passed to the AI model. The model produces prediction values that are converted into human-readable prediction and confidence percentages.

## Backend Setup

### Prerequisites

Make sure the following are installed:

* Java 21
* Maven
* MySQL
* Node.js and npm
* IntelliJ IDEA or another Java IDE

### 1. Clone the repository

```bash
git clone https://github.com/Nithesh-19/Ai-Image-Detector.git
```

### 2. Configure MySQL

Create a database:

```sql
CREATE DATABASE ai_detector;
```

The backend expects MySQL to be available locally.

### 3. Configure the database password

The application uses an environment variable for the database password.

```properties
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD}
```

Set the environment variable before starting the backend.

On Windows PowerShell:

```powershell
$env:DB_PASSWORD="your-password"
```

Do not commit your actual database password to GitHub.

### 4. Start the Spring Boot backend

Navigate to the backend directory:

```bash
cd ai-content-detector
```

Run:

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

The backend runs on:

```text
http://localhost:8080
```

## Frontend Setup

Open another terminal and navigate to:

```bash
cd frontend
```

Install dependencies:

```bash
npm install
```

Start the React development server:

```bash
npm run dev
```

Vite will provide the local frontend URL in the terminal.

Open that URL in your browser.

## API

The backend exposes REST endpoints for image detection.

Example:

```text
POST /api/detect
```

The endpoint accepts an image upload and returns the detection result.

Example response:

```json
{
  "prediction": "ARTIFICIAL",
  "artificialPercentage": 99.99,
  "humanPercentage": 0.01
}
```

## Current Model

The current version uses the **Detectra ONNX model** for image detection.

The model is loaded by the Spring Boot backend and used during image analysis.

## Security

Sensitive configuration such as the database password is not stored directly in the repository.

The application uses:

```text
DB_PASSWORD
```

as an environment variable.

## Future Improvements

Planned improvements include:

* Add additional AI detection models
* Improve model selection
* Add video detection
* Improve image preprocessing
* Deploy the application to the cloud
* Improve authentication and authorization
* Add more detailed detection history
* Improve UI/UX
* Add automated testing

## Screenshots

Screenshots of the application will be added here.

## Author

**Nithesh K**

Java Full Stack Developer — Fresher

GitHub:
https://github.com/Nithesh-19
