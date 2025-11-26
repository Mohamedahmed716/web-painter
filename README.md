# PaintMaster - Vector Graphics Application

PaintMaster is a full-stack vector graphics editor built with **Angular
17+** on the frontend and **Spring Boot** on the backend. It allows
users to draw geometric shapes, manipulate them (move, resize, copy,
delete), and save/load their work using JSON or XML formats.

## 🚀 Features

### Drawing Tools

-   Freehand (Pencil): Draw free-form lines.
-   Shapes: Line Segment, Circle, Rectangle, Square, Triangle, Ellipse.

### Manipulation & Editing

-   Select & Move: Click to select a shape and drag to move it.
-   Resize: Drag the handle on the bottom-right corner of a selected
    shape.
-   Copy & Paste:
    1.  Select a shape\
    2.  Click "Copy"\
    3.  Click anywhere on the canvas to paste the duplicate
-   Delete: Remove selected shapes or clear the entire board.
-   Undo / Redo: Supports full history traversal.

### Styling

-   Stroke Color: Change outline color.
-   Fill Color: Add solid fill color to closed shapes.
-   Stroke Width: Adjust border and line thickness.

### Persistence

-   Save as `.json` or `.xml`
-   Load previously exported files

## 🛠️ Tech Stack

### Frontend

-   Angular 17+ (Standalone Components)
-   RxJS BehaviorSubjects
-   CSS3 (Flexbox, Variables)
-   HTML5 Canvas API

### Backend

-   Spring Boot 3+
-   Java 17+
-   Jackson (JSON/XML)
-   Service + Controller architecture with Singleton state

## 📡 RESTful API Architecture

PaintMaster uses a simple REST API. The frontend sends user actions and
receives updated shape lists or state changes.

**Base URL:** `http://localhost:8080/api`

### Available Endpoints

-   `GET /shapes` --- Get all shapes
-   `POST /create` --- Create a new shape\
    Example body: `{ "type": "circle", "params": { ... } }`
-   `POST /select` --- Select shape at coordinates\
    Example: `{ "x": 100, "y": 200 }`
-   `POST /move` --- Move selected shape\
    Example: `{ "dx": 10, "dy": -5 }`
-   `POST /resize` --- Resize selected shape\
    Example: `{ "anchor": "bottom-right", "dx": 5, "dy": 5 }`
-   `POST /copy` --- Copy selected shape
-   `POST /paste` --- Paste copy at coordinates\
    Example: `{ "x": 150, "y": 150 }`
-   `POST /delete` --- Delete selected shape
-   `POST /clear` --- Clear all shapes
-   `POST /undo` --- Undo last action
-   `POST /redo` --- Redo action
-   `POST /color` --- Change stroke color\
    Example: `{ "color": "#ff0000" }`
-   `POST /fill` --- Change fill color\
    Example: `{ "fillColor": "#00ff00" }`
-   `POST /width` --- Change stroke width\
    Example: `{ "width": 5 }`
-   `GET /save/{format}` --- Download JSON or XML
-   `POST /load` --- Upload and restore file (`multipart/form-data`)

## ⚙️ Setup & Installation

### Prerequisites

-   Node.js 18+
-   npm
-   Java JDK 17+
-   Maven

### Backend Setup

1.  Navigate to the backend folder\
2.  Run:

```{=html}
<!-- -->
```
    mvn spring-boot:run

Backend will start on:\
`http://localhost:8080`

### Frontend Setup

1.  Navigate to the frontend folder\
2.  Install dependencies:

```{=html}
<!-- -->
```
    npm install

3.  Start the dev server:

```{=html}
<!-- -->
```
    ng serve

Frontend runs at:\
`http://localhost:4200`

## 📖 How to Use

-   Select a tool from the sidebar and draw on the canvas.
-   Switch to the Select Tool to highlight a shape (blue outline).
-   Drag to move selected shapes.
-   Resize using the bottom-right white handle.
-   Copy → click anywhere → Paste to duplicate a shape.
-   Change stroke, fill color, or width using the top toolbar.
-   Save drawings as JSON or XML.
-   Load a saved file to restore the canvas.

## 📐 Design Patterns

PaintMaster uses several OOP design patterns:

-   Factory Pattern: Creates all shape objects.
-   Prototype Pattern: `clone()` enables deep copying.
-   Singleton Pattern: A single PaintService stores state.
