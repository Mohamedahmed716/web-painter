import { Component, ElementRef, ViewChild, AfterViewInit, Input, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../services/api';
import { Shape } from '../../models/shape';
import { DrawingService } from '../../services/drawing';

@Component({
  selector: 'app-board',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './board.component.html',
  styleUrl: './board.component.css',
})
export class BoardComponent implements AfterViewInit {
  @ViewChild('canvas') canvasRef!: ElementRef<HTMLCanvasElement>;
  private ctx!: CanvasRenderingContext2D;
  private apiService = inject(ApiService);
  private drawingService = inject(DrawingService);
  selectedShapeId: string | null = null;
  isPasting = false;
  copiedShapeId: string | null = null;



  @Input() currentTool: string = 'circle';
  @Input() currentColor: string = '#000000';
  shapes: Shape[] = [];

  isDrawing = false;
  startX = 0;
  startY = 0;
  freehandPoints: { x: number; y: number }[] = [];
  ngAfterViewInit(): void {
    this.drawingService.colorChange$.subscribe(shapes => {
  this.shapes = shapes;

  // ⭐ update the selected shape ID
  const selected = shapes.find((s: any) => s.selected);
  this.selectedShapeId = selected?.id ?? null;

  this.redrawAll();   // ⭐ immediately redraw canvas
     });


     this.drawingService.copy$.subscribe(() => this.copySelected());

    this.drawingService.delete$.subscribe(() => this.deleteSelected());
    this.ctx = this.canvasRef.nativeElement.getContext('2d')!;
    this.drawingService.currentTool$.subscribe((tool) => (this.currentTool = tool));
    this.drawingService.currentColor$.subscribe((color) => (this.currentColor = color));
    this.drawingService.undo$.subscribe(() => this.undo());
    this.drawingService.redo$.subscribe(() => this.redo());
    this.refreshBoard();
  }

onMouseDown($event: MouseEvent) {
  const x = $event.offsetX;
  const y = $event.offsetY;

  // =====================================
  // SELECT TOOL (SELECT + START DRAG)
  // =====================================
  if (this.currentTool === 'select') {

    // 1️⃣ START UNDO SNAPSHOT BEFORE ANYTHING ELSE
    this.apiService.startMove().subscribe(); 

    // setup dragging state
    this.isMoving = true;
    this.lastX = x;
    this.lastY = y;

    // 2️⃣ TELL BACKEND WHICH SHAPE TO SELECT
    this.apiService.select(x, y).subscribe(shapes => {
    this.shapes = shapes;

    // ⭐ store selected ID from backend
    const selected = shapes.find((s: any) => s.selected);
    this.selectedShapeId = selected?.id ?? null;

    this.redrawAll();
    });
    return;  // IMPORTANT → prevents drawing logic
  }

  // =====================================
  // NORMAL DRAWING TOOLS
  // =====================================
  this.isDrawing = true;
  this.startX = x;
  this.startY = y;

  if (this.currentTool === 'freehand') {
    this.freehandPoints = [{ x: this.startX, y: this.startY }];
  }
  if (this.currentTool === 'resize') {
  this.isResizing = true;

  this.lastX = x;
  this.lastY = y;

  // Start ONE undo snapshot
  this.apiService.startMove().subscribe();

  return;
}
  // ==========================
  // PASTE MODE: copy at click
  // ==========================
  if (this.isPasting) {
    this.isPasting = false; // consume paste mode

    // 1) Tell backend to create a copy (it will select the new copy)
    this.apiService.copySelected().subscribe(shapesAfterCopy => {
      this.shapes = shapesAfterCopy;

      // Find the newly copied shape (selected = true from backend)
      const copied = shapesAfterCopy.find((s: any) => s.selected === true);

      if (!copied) {
        this.redrawAll();
        return;
      }

      // 2) Compute how much to move the new copy
      const dx = x - copied.x;
      const dy = y - copied.y;

      // 3) Move the new copy so that its position goes to the click location
      this.apiService.moveSelected(dx, dy).subscribe(shapesAfterMove => {
        this.shapes = shapesAfterMove;

        const selected = shapesAfterMove.find((s: any) => s.selected === true);
        this.selectedShapeId = selected?.id ?? null;

        this.redrawAll();
      });
    });

    return; // important: don't fall into normal drawing/select logic
  }

  
}




  onMouseMove($event: MouseEvent) {
  const x = $event.offsetX;
  const y = $event.offsetY;

  // ================================
  // MOVE SELECTED SHAPE
  // ================================
  if (this.currentTool === 'select' && this.isMoving) {

    const dx = x - this.lastX;
    const dy = y - this.lastY;

    this.lastX = x;
    this.lastY = y;

    this.apiService.moveSelected(dx, dy).subscribe(shapes => {
      this.shapes = shapes;
      this.redrawAll();
    });


    return;
  }

  // ================================
  // NORMAL DRAWING LOGIC
  // ================================
  if (!this.isDrawing) return;

  if (this.currentTool === 'freehand') {
    this.freehandPoints.push({ x, y });
  }

  this.redrawAll();
  this.drawPreview(this.startX, this.startY, x, y);
  if (this.currentTool === 'resize' && this.isResizing) {
  const dx = x - this.lastX;
  const dy = y - this.lastY;

  this.lastX = x;
  this.lastY = y;

  this.apiService.resizeSelected(this.resizeAnchor, dx, dy)
    .subscribe(shapes => {
      this.shapes = shapes;
      this.redrawAll();
    });

  return;
}
}



  onMouseUp($event: MouseEvent) {

  // ================================
  // STOP DRAGGING SHAPE
  // ================================
  if (this.currentTool === 'select') {
    this.isMoving = false;

    // End movement snapshot (ONE undo record)
    this.apiService.endMove().subscribe(shapes => {
      this.shapes = shapes;
      this.redrawAll();
    });

    return; // IMPORTANT
  }

  // ================================
  // NORMAL DRAWING LOGIC
  // ================================
  if (!this.isDrawing) return;
  this.isDrawing = false;

  const type = this.currentTool;
  const params: any = { color: this.currentColor, type: type };

  if (type === 'freehand') {
    params.points = this.freehandPoints;
  } else {
    params.x1 = this.startX;
    params.y1 = this.startY;
    params.x2 = $event.offsetX;
    params.y2 = $event.offsetY;
  }

  this.apiService.createShape(type, params).subscribe({
    next: (data) => {
      this.shapes = data;
      this.freehandPoints = [];
      this.redrawAll();
    },
    error: (err) => console.error('Error creating shape', err),
  });
  // ===============================
// END RESIZING
// ===============================
if (this.currentTool === 'resize' && this.isResizing) {
  this.isResizing = false;

  this.apiService.endMove().subscribe(shapes => {
    this.shapes = shapes;
    this.redrawAll();
  });

  return;
}


}



  refreshBoard() {
    this.apiService.getShapes().subscribe({
      next: (data) => {
        this.shapes = data;
        this.redrawAll();
      },
      error: (err) => console.error('Error fetching shapes', err),
    });
  }

 redrawAll() {
  this.ctx.clearRect(
    0,
    0,
    this.canvasRef.nativeElement.width,
    this.canvasRef.nativeElement.height
  );

  this.shapes.forEach((shape: any) => {
  this.drawShape(shape);
  if (shape.selected || shape.id === this.selectedShapeId) {
    this.drawSelectionOutline(this.ctx, shape);
  }
});
}


  drawShape(s: Shape) {
    this.ctx.beginPath();
    this.ctx.strokeStyle = s.color;
    this.ctx.lineWidth = 2;

    if (s.type === 'circle') {
      this.ctx.arc(s.x, s.y, s.radius!, 0, 2 * Math.PI);
    } else if (s.type === 'rectangle' || s.type === 'square') {
      this.ctx.rect(s.x, s.y, s.width!, s.height!);
    } else if (s.type === 'line') {
      this.ctx.moveTo(s.x, s.y);
      this.ctx.lineTo((s as any).x2, (s as any).y2);
    } else if (s.type === 'ellipse') {
      this.ctx.ellipse(s.x, s.y, (s as any).radiusX, (s as any).radiusY, 0, 0, 2 * Math.PI);
    } else if (s.type === 'triangle') {
      const t = s as any;
      this.ctx.moveTo(t.x, t.y);
      this.ctx.lineTo(t.x2, t.y2);
      this.ctx.lineTo(t.x3, t.y3);
      this.ctx.closePath();
    } else if (s.type === 'freehand' && (s as any).points) {
      const points = (s as any).points;
      if (points && points.length > 0) {
        this.ctx.moveTo(points[0].x, points[0].y);
        for (let i = 1; i < points.length; i++) {
          this.ctx.lineTo(points[i].x, points[i].y);
        }
      }
    }
    this.ctx.stroke();
  }

  drawPreview(x1: number, y1: number, x2: number, y2: number) {
    this.ctx.beginPath();
    this.ctx.strokeStyle = this.currentTool === 'freehand' ? this.currentColor : 'gray';
    this.ctx.setLineDash(this.currentTool === 'freehand' ? [] : [5, 5]);

    if (this.currentTool === 'circle') {
      const r = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
      this.ctx.arc(x1, y1, r, 0, 2 * Math.PI);
    } else if (this.currentTool === 'rectangle') {
      this.ctx.rect(x1, y1, x2 - x1, y2 - y1);
    } else if (this.currentTool === 'square') {
      const side = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
      const rectX = x2 < x1 ? x1 - side : x1;
      const rectY = y2 < y1 ? y1 - side : y1;
      this.ctx.rect(rectX, rectY, side, side);
    } else if (this.currentTool === 'line') {
      this.ctx.moveTo(x1, y1);
      this.ctx.lineTo(x2, y2);
    } else if (this.currentTool === 'ellipse') {
      const rx = Math.abs(x2 - x1) / 2;
      const ry = Math.abs(y2 - y1) / 2;
      const cx = Math.min(x1, x2) + rx;
      const cy = Math.min(y1, y2) + ry;
      this.ctx.ellipse(cx, cy, rx, ry, 0, 0, 2 * Math.PI);
    } else if (this.currentTool === 'triangle') {
      this.ctx.moveTo((x1 + x2) / 2, y1);
      this.ctx.lineTo(x1, y2);
      this.ctx.lineTo(x2, y2);
      this.ctx.closePath();
    } else if (this.currentTool === 'freehand') {
      if (this.freehandPoints.length > 0) {
        this.ctx.moveTo(this.freehandPoints[0].x, this.freehandPoints[0].y);
        for (let i = 1; i < this.freehandPoints.length; i++) {
          this.ctx.lineTo(this.freehandPoints[i].x, this.freehandPoints[i].y);
        }
      }
    }
    this.ctx.stroke();
    this.ctx.setLineDash([]);
  }

  undo() {
    this.apiService.undo().subscribe({
      next: (shapes) => {
        this.shapes = shapes;
        this.refreshBoard();
      },
      error: (err) => console.error(err),
    });
  }

  redo() {
    this.apiService.redo().subscribe({
      next: (shapes) => {
        this.shapes = shapes;
        this.refreshBoard();
      },
      error: (err) => console.error(err),
    });
  }
  deleteSelected() {
  this.apiService.deleteSelected().subscribe(shapes => {
    this.shapes = shapes;

    // ⭐ FIX: clear selection after delete
    this.selectedShapeId = null;

    this.redrawAll();
  });
}
copySelected() {
  this.apiService.copySelected().subscribe(shapes => {
    this.shapes = shapes;

    const selected = shapes.find((s: any) => s.selected === true);
    this.selectedShapeId = selected?.id ?? null;

    this.redrawAll();
  });
}





private drawSelectionOutline(ctx: CanvasRenderingContext2D, shape: any) {
  const box = this.getBoundingBox(shape);

  ctx.save();
  ctx.strokeStyle = "#007bff";
  ctx.lineWidth = 2;
  ctx.setLineDash([6, 4]);
  ctx.strokeRect(box.x - 4, box.y - 4, box.width + 8, box.height + 8);
  ctx.restore();

}

private getBoundingBox(
  shape: any
): { x: number; y: number; width: number; height: number } {
  switch (shape.type) {

    case 'rectangle':
      return {
        x: shape.x,
        y: shape.y,
        width: shape.width,
        height: shape.height
      };

    case 'square':
      return {
        x: shape.x,
        y: shape.y,
        width: shape.sideLength,
        height: shape.sideLength
      };

    case 'circle':
      return {
        x: shape.x - shape.radius,
        y: shape.y - shape.radius,
        width: shape.radius * 2,
        height: shape.radius * 2
      };

    case 'ellipse':
      return {
        x: shape.x - shape.radiusX,
        y: shape.y - shape.radiusY,
        width: shape.radiusX * 2,
        height: shape.radiusY * 2
      };

    case 'line':
      return {
        x: Math.min(shape.x, shape.x2),
        y: Math.min(shape.y, shape.y2),
        width: Math.abs(shape.x2 - shape.x),
        height: Math.abs(shape.y2 - shape.y)
      };

    case 'triangle':
      const minX = Math.min(shape.x, shape.x2, shape.x3);
      const minY = Math.min(shape.y, shape.y2, shape.y3);
      const maxX = Math.max(shape.x, shape.x2, shape.x3);
      const maxY = Math.max(shape.y, shape.y2, shape.y3);
      return {
        x: minX,
        y: minY,
        width: maxX - minX,
        height: maxY - minY
      };

    case 'freehand':
      let xs = shape.points.map((p: any) => p.x);
      let ys = shape.points.map((p: any) => p.y);
      return {
        x: Math.min(...xs),
        y: Math.min(...ys),
        width: Math.max(...xs) - Math.min(...xs),
        height: Math.max(...ys) - Math.min(...ys)
      };
      default:
          return { x: 0, y: 0, width: 0, height: 0 };

  }
}
startPasteMode() {
  this.isPasting = true;
  this.copiedShapeId = this.selectedShapeId; // store which shape was copied
}



  // --- Selection / Move state ---
selected = false;
isMoving = false;
lastX = 0;
lastY = 0;
isResizing = false;
resizeAnchor = "both";


}
