import { Component, ElementRef, ViewChild, AfterViewInit, Input, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../services/api';
import { Shape } from '../../models/shape';

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

  @Input() currentTool: string = 'circle';
  @Input() currentColor: string = '#000000';

  shapes: Shape[] = [];
  isDrawing = false;
  startX = 0;
  startY = 0;

  ngAfterViewInit(): void {
    this.ctx = this.canvasRef.nativeElement.getContext('2d')!;
    this.refreshBoard();
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
      this.ctx.moveTo(t.x, t.y); // Top point of the triangle
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
}
