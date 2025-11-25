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

  @Input() currentTool: string = 'line';
  @Input() currentColor: string = '#000000';
  shapes: Shape[] = [];

  isDrawing = false;
  startX = 0;
  startY = 0;

  ngAfterViewInit(): void {
    this.ctx = this.canvasRef.nativeElement.getContext('2d')!;
    this.drawingService.currentTool$.subscribe((tool) => (this.currentTool = tool));
    this.drawingService.currentColor$.subscribe((color) => (this.currentColor = color));
    this.drawingService.undo$.subscribe(() => this.undo());
    this.drawingService.redo$.subscribe(() => this.redo());
    this.refreshBoard();
  }

  onMouseDown($event: MouseEvent) {
    this.isDrawing = true;
    this.startX = $event.offsetX;
    this.startY = $event.offsetY;

    // Initialize freehand points
    if (this.currentTool === 'freehand') {
      this.freehandPoints = [{ x: this.startX, y: this.startY }];
    }
  }

  onMouseMove($event: MouseEvent) {
    if (!this.isDrawing) return;

    // For freehand, we keep adding points to the array
    if (this.currentTool === 'freehand') {
      this.freehandPoints.push({ x: $event.offsetX, y: $event.offsetY });
    }

    this.redrawAll();
    this.drawPreview(this.startX, this.startY, $event.offsetX, $event.offsetY);
  }

  onMouseUp($event: MouseEvent) {
    if (!this.isDrawing) return;
    this.isDrawing = false;

    const type = this.currentTool;
    const params: any = {
      color: this.currentColor,
      type: type,
    };

    // Handle Freehand Payload
    if (type === 'freehand') {
      // Send the entire array of points
      params.points = this.freehandPoints;
    } else {
      // Standard Shapes (Start/End points)
      params.x1 = this.startX;
      params.y1 = this.startY;
      params.x2 = $event.offsetX;
      params.y2 = $event.offsetY;
    }

    this.apiService.createShape(type, params).subscribe({
      next: (data) => {
        this.shapes = data;
        this.redrawAll();
        // Clear points after save
        this.freehandPoints = [];
      },
      error: (err) => console.error('Error creating shape', err),
    });
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
    this.shapes.forEach((shape) => this.drawShape(shape));
  }

  // --- RENDERING ---

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
    }
    // Handle drawing saved freehand shapes (Assumes backend returns 'points')
    else if (s.type === 'freehand' && (s as any).points) {
      const points = (s as any).points;
      if (points.length > 0) {
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

    // Only dash the line for geometric shapes, not freehand
    if (this.currentTool !== 'freehand') {
      this.ctx.setLineDash([5, 5]);
    } else {
      this.ctx.setLineDash([]);
    }

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
    }
    // Logic for Freehand Preview
    else if (this.currentTool === 'freehand') {
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
      error: (err) => console.error('Error performing undo', err),
    });
  }

  redo() {
    this.apiService.redo().subscribe({
      next: (shapes) => {
        this.shapes = shapes;
        this.refreshBoard();
      },
      error: (err) => console.error('Error performing redo', err),
    });
  }
}
