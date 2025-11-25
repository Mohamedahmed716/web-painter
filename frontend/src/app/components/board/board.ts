import {Component, ElementRef, ViewChild, AfterViewInit, Input, inject, Injector} from '@angular/core';
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

  @Input() currentTool: string = 'circle';
  @Input() currentColor: string = '#000000';
  shapes: Shape[] = [];

  isDrawing = false;
  startX = 0;
  startY = 0;

  ngAfterViewInit(): void {
    this.ctx = this.canvasRef.nativeElement.getContext('2d')!;
    this.drawingService.currentTool$.subscribe(tool => this.currentTool = tool);
    this.drawingService.currentColor$.subscribe(color => this.currentColor = color);
    this.drawingService.undo$.subscribe(() => this.undo());
    this.drawingService.redo$.subscribe(() => this.redo());
    this.refreshBoard();
  }

  onMouseDown($event: MouseEvent) {
    this.isDrawing = true;
    this.startX = $event.offsetX;
    this.startY = $event.offsetY;
  }

  onMouseMove($event: MouseEvent) {
    if(!this.isDrawing) return;
    this.redrawAll();
    this.drawPreview(this.startX, this.startY, $event.offsetX, $event.offsetY);
  }

  onMouseUp($event: MouseEvent) {
    if (!this.isDrawing) return;
    this.isDrawing = false;

    const type = this.currentTool;
    const currentX = $event.offsetX;
    const currentY = $event.offsetY;
    const params: any = {
      color: this.currentColor,
      type: type
    };

    if (type === 'circle') {
      params.x = this.startX;
      params.y = this.startY;
      params.radius = Math.sqrt(Math.pow(currentX - this.startX, 2) + Math.pow(currentY - this.startY, 2));
    }
    else if (type === 'rectangle') {
      params.x = Math.min(this.startX, currentX);
      params.y = Math.min(this.startY, currentY);
      params.width = Math.abs(currentX - this.startX);
      params.height = Math.abs(currentY - this.startY);
    }
    else if (type === 'square') {
      const w = Math.abs(currentX - this.startX);
      const h = Math.abs(currentY - this.startY);
      const side = Math.max(w, h);

      params.x = (currentX < this.startX) ? this.startX - side : this.startX;
      params.y = (currentY < this.startY) ? this.startY - side : this.startY;
      params.width = side;
      params.height = side;
    }
    else if (type === 'line') {
      params.x = this.startX;
      params.y = this.startY;
      params.x2 = currentX;
      params.y2 = currentY;
    }
    else if (type === 'ellipse') {
      params.x = (this.startX + currentX) / 2;
      params.y = (this.startY + currentY) / 2;
      params.radiusX = Math.abs(currentX - this.startX) / 2;
      params.radiusY = Math.abs(currentY - this.startY) / 2;
    }
    else if (type === 'triangle') {
      params.x = (this.startX + currentX) / 2;
      params.y = this.startY;
      params.x2 = this.startX;
      params.y2 = currentY;
      params.x3 = currentX;
      params.y3 = currentY;
    }

    this.apiService.createShape(type, params).subscribe({
      next: (newShapeOrList) => {
        this.shapes = newShapeOrList;
        this.redrawAll();
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

  // Rendering Shapes

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
      this.ctx.moveTo(t.x, t.y); // Top point
      this.ctx.lineTo(t.x2, t.y2);
      this.ctx.lineTo(t.x3, t.y3);
      this.ctx.closePath();
    }

    this.ctx.stroke();
  }

  drawPreview(x1: number, y1: number, x2: number, y2: number) {
    this.ctx.beginPath();
    this.ctx.strokeStyle = 'gray';
    this.ctx.setLineDash([5, 5]);

    if (this.currentTool === 'circle') {
      const r = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
      this.ctx.arc(x1, y1, r, 0, 2 * Math.PI);
    } else if (this.currentTool === 'rectangle') {
      this.ctx.rect(x1, y1, x2 - x1, y2 - y1);
    } else if (this.currentTool === 'square') {
      const side = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
      const rectX = (x2 < x1) ? x1 - side : x1;
      const rectY = (y2 < y1) ? y1 - side : y1;
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

    this.ctx.stroke();
    this.ctx.setLineDash([]);
  }

  undo(){
    this.apiService.undo().subscribe({
      next: (shapes) => {
        this.shapes = shapes;
        this.refreshBoard();
      },
      error: (err) => console.error('Error performing undo', err),
    });
  }

  redo(){
    this.apiService.redo().subscribe({
      next: (shapes) => {
        this.shapes = shapes;
        this.refreshBoard();
      },
      error: (err) => console.error('Error performing redo', err),
    });
  }
}
