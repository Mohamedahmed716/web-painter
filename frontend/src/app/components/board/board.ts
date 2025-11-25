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
    if(!this.isDrawing) return;
    this.isDrawing = false;
    const type = this.currentTool;
    const params: any = {};
    if (type === 'circle') {
      const r = Math.sqrt(Math.pow($event.offsetX - this.startX, 2) + Math.pow($event.offsetY - this.startY, 2));
      params.x = this.startX;
      params.y = this.startY;
      params.radius = r;
    }
    else if (type === 'rectangle' || type === 'square') {
      params.x = this.startX;
      params.y = this.startY;
      params.width = $event.offsetX - this.startX;
      params.height = $event.offsetY - this.startY;
    }
    else if (type === 'line') {
      params.x = this.startX;
      params.y = this.startY;
      params.x2 = $event.offsetX;
      params.y2 = $event.offsetY;
    }
    // probably not right
    else if (type === 'ellipse') {
      params.x = (this.startX + $event.offsetX) / 2;
      params.y = (this.startY + $event.offsetY) / 2;
      params.radiusX = Math.abs($event.offsetX - this.startX) / 2;
      params.radiusY = Math.abs($event.offsetY - this.startY) / 2;
    }
    else if (type === 'triangle') {
      params.x = (this.startX + $event.offsetX) / 2;
      params.y = this.startY;
      params.x2 = this.startX;
      params.y2 = $event.offsetY;
      params.x3 = $event.offsetX;
      params.y3 = $event.offsetY;
    }
    params.color = this.currentColor;

    this.apiService.createShape(type, params).subscribe({
      next: (shapes) => {
        this.shapes = shapes;
        this.refreshBoard();
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
    } else if (this.currentTool === 'rectangle' || this.currentTool === 'square') {
      this.ctx.rect(x1, y1, x2 - x1, y2 - y1);
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
