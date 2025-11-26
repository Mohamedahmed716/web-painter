# PaintMaster

PaintMaster - Vector Graphics Application

PaintMaster is a full-stack vector graphics editor built with Angular 17+ (Frontend) and Spring Boot (Backend). It allows users to draw geometric shapes, manipulate them (move, resize, copy, delete), and persist their work using JSON or XML formats.

🚀 Features

Drawing Tools

Freehand (Pencil): Draw free-form lines.

Shapes: Line Segment, Circle, Rectangle, Square, Triangle, Ellipse.

Manipulation & Editing

Select & Move: Click to select a shape and drag to move it.

Resize: Drag the handle on the bottom-right corner of a selected shape to resize it.

Copy & Paste: 1. Select a shape.
2. Click Copy (stores the shape).
3. Click anywhere on the board to Paste a duplicate at that location.

Delete: Remove selected shapes or Clear Board to reset everything.

Undo / Redo: Robust history management for all actions.

Styling

Stroke Color: Change the outline color of any shape.

Fill Color: Fill closed shapes (Circle, Rect, Triangle, etc.) with a solid color.

Stroke Width: Adjust the thickness of lines and borders using the slider.

Persistence

Save: Export drawings as .json or .xml files.

Load: Upload previously saved files to restore the canvas state.

🛠️ Tech Stack

Frontend

Framework: Angular 17+ (Standalone Components)

State Management: RxJS (BehaviorSubjects)

Styling: CSS3 (Flexbox, CSS Variables)

Canvas: HTML5 Canvas API

Backend

Framework: Spring Boot 3+

Language: Java 17+

Data Handling: Jackson (JSON/XML Serialization)

Architecture: Service-Controller Layer with Singleton State Management

⚙️ Setup & Installation

Prerequisites

Node.js (v18+) & npm

Java JDK 17+

Maven

1. Backend Setup

Navigate to the backend folder.

Run the application using Maven:

mvn spring-boot:run


The server will start at http://localhost:8080.

2. Frontend Setup

Navigate to the frontend folder.

Install dependencies:

npm install


Start the development server:

ng serve


Open your browser at http://localhost:4200.

📖 How to Use

Drawing: Select a tool from the left sidebar and drag on the white canvas to draw.

Selecting: Switch to the Select Tool (Cursor icon). Click on any shape to select it (blue outline appears).

Moving: Drag a selected shape to move it.

Resizing: Select a shape, then drag the white square handle at the bottom-right corner.

Copy/Paste:

Select a shape.

Click the Copy icon (Paper).

Click anywhere on the canvas to Paste the copy.

Styling: Use the top bar controls to change Stroke Color, Fill Color, or Width. Changes apply to the selected shape immediately.

Saving: Click Save in the top right and choose JSON or XML.

Loading: Click the Folder icon to upload a saved file.

📐 Design Patterns

This project implements several key OOP design patterns:

Factory Pattern: The ShapeFactory class encapsulates the logic for creating different shape objects (Circle, Rectangle, FreehandShape) from raw input data.

Prototype Pattern: The clone() method in the Shape class hierarchy allows for deep copying of objects, which is essential for the Copy/Paste feature and the Undo/Redo history stacks.

Singleton Pattern: The PaintService in Spring Boot acts as a singleton, maintaining the single source of truth for the application state (the list of shapes) across multiple API requests.

Memento Pattern (Variant): The Undo/Redo functionality uses stacks to save snapshots (
