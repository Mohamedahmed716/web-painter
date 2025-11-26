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

  @Input() currentTool: string = 'circle';
  @Input() currentColor: string = '#000000';
  shapes: Shape[] = [];

  isDrawing = false; isMoving = false; isResizing = false; isPasting = false;
  startX = 0; startY = 0; lastX = 0; lastY = 0;
  selectedShapeId: string | null = null;
  freehandPoints: { x: number; y: number }[] = [];
  resizeAnchor = 'bottom-right';

  ngAfterViewInit(): void {
    this.ctx = this.canvasRef.nativeElement.getContext('2d')!;
    this.drawingService.currentTool$.subscribe(tool => this.currentTool = tool);
    this.drawingService.currentColor$.subscribe(color => this.currentColor = color);
    this.drawingService.undo$.subscribe(() => this.undo());
    this.drawingService.redo$.subscribe(() => this.redo());
    this.drawingService.delete$.subscribe(() => this.deleteSelected());
    this.drawingService.copy$.subscribe(() => { this.apiService.copySelected().subscribe(); this.isPasting = true; });
    this.drawingService.colorChange$.subscribe(shapes => { this.shapes = shapes; this.redrawAll(); });
    this.refreshBoard();
  }

  onMouseDown($event: MouseEvent) {
    const x = $event.offsetX; const y = $event.offsetY;

    if (this.isPasting) {
        this.isPasting = false;
        this.apiService.pasteSelected(x, y).subscribe(shapes => {
            this.shapes = shapes;
            if(this.shapes.length > 0) this.selectedShapeId = this.shapes[this.shapes.length - 1].id!;
            this.redrawAll();
        });
        return;
    }

    if (this.currentTool === 'select') {
        if (this.selectedShapeId) {
            const handle = this.getHandleAt(x, y);
            if (handle) {
                this.isResizing = true; this.resizeAnchor = handle;
                this.lastX = x; this.lastY = y;
                this.apiService.startMove().subscribe();
                return;
            }
        }
        this.apiService.select(x, y).subscribe(shapes => {
            this.shapes = shapes;
            const found = this.findShapeAt(x, y);
            this.selectedShapeId = found ? found.id! : null;
            if (this.selectedShapeId) {
                this.isMoving = true; this.lastX = x; this.lastY = y;
                this.apiService.startMove().subscribe();
            }
            this.redrawAll();
        });
        return;
    }

    if (this.currentTool === 'resize') {
        if (this.selectedShapeId) {
            this.isResizing = true; this.lastX = x; this.lastY = y;
            this.apiService.startMove().subscribe();
        }
        return;
    }

    this.isDrawing = true; this.startX = x; this.startY = y;
    if (this.currentTool === 'freehand') this.freehandPoints = [{ x, y }];
  }

  onMouseMove($event: MouseEvent) {
    const x = $event.offsetX; const y = $event.offsetY;

    if (this.currentTool === 'select' && this.isMoving) {
        this.apiService.moveSelected(x - this.lastX, y - this.lastY).subscribe(shapes => { this.shapes = shapes; this.redrawAll(); });
        this.lastX = x; this.lastY = y; return;
    }
    if ((this.currentTool === 'select' || this.currentTool === 'resize') && this.isResizing) {
        this.apiService.resizeSelected(this.resizeAnchor, x - this.lastX, y - this.lastY).subscribe(shapes => { this.shapes = shapes; this.redrawAll(); });
        this.lastX = x; this.lastY = y; return;
    }
    if (!this.isDrawing) return;
    if (this.currentTool === 'freehand') this.freehandPoints.push({ x, y });
    this.redrawAll(); this.drawPreview(this.startX, this.startY, x, y);
  }

  onMouseUp($event: MouseEvent) {
    if (this.isMoving || this.isResizing) {
        this.isMoving = false; this.isResizing = false;
        this.apiService.endMove().subscribe(shapes => { this.shapes = shapes; this.redrawAll(); });
        return;
    }
    if (!this.isDrawing) return;
    this.isDrawing = false;
    const type = this.currentTool;
    const params: any = { color: this.currentColor, type: type, strokeWidth: 2.0 };
    if (type === 'freehand') params.points = this.freehandPoints;
    else { params.x1 = this.startX; params.y1 = this.startY; params.x2 = $event.offsetX; params.y2 = $event.offsetY; }

    this.apiService.createShape(type, params).subscribe({
      next: (data) => {
        this.shapes = data; this.freehandPoints = [];
        if(this.shapes.length > 0) this.selectedShapeId = this.shapes[this.shapes.length - 1].id!;
        this.redrawAll();
      },
      error: (err) => console.error(err),
    });
  }

  refreshBoard() { this.apiService.getShapes().subscribe(data => { this.shapes = data; this.redrawAll(); }); }
  deleteSelected() { this.apiService.deleteSelected().subscribe(shapes => { this.shapes = shapes; this.selectedShapeId = null; this.redrawAll(); }); }
  undo() { this.apiService.undo().subscribe(shapes => { this.shapes = shapes; this.redrawAll(); }); }
  redo() { this.apiService.redo().subscribe(shapes => { this.shapes = shapes; this.redrawAll(); }); }

  redrawAll() {
    this.ctx.clearRect(0, 0, this.canvasRef.nativeElement.width, this.canvasRef.nativeElement.height);
    this.shapes.forEach(shape => {
        this.drawShape(shape);
        if (shape.id === this.selectedShapeId) this.drawSelectionOutline(shape);
    });
  }

  drawShape(s: Shape) {
    this.ctx.beginPath();
    this.ctx.strokeStyle = s.color;
    this.ctx.lineWidth = s.strokeWidth || 2;

    if (s.fillColor && s.fillColor !== 'transparent' && s.fillColor !== 'none') this.ctx.fillStyle = s.fillColor;
    else this.ctx.fillStyle = 'rgba(0,0,0,0)';

    if (s.type === 'circle') this.ctx.arc(s.x, s.y, s.radius!, 0, 2 * Math.PI);
    else if (s.type === 'rectangle' || s.type === 'square') this.ctx.rect(s.x, s.y, s.width!, s.height!);
    else if (s.type === 'line') { this.ctx.moveTo(s.x, s.y); this.ctx.lineTo((s as any).x2, (s as any).y2); }
    else if (s.type === 'ellipse') this.ctx.ellipse(s.x, s.y, (s as any).radiusX, (s as any).radiusY, 0, 0, 2 * Math.PI);
    else if (s.type === 'triangle') {
      const t = s as any; this.ctx.moveTo(t.x, t.y); this.ctx.lineTo(t.x2, t.y2); this.ctx.lineTo(t.x3, t.y3); this.ctx.closePath();
    } else if (s.type === 'freehand' && (s as any).points) {
      const points = (s as any).points;
      if (points && points.length > 0) {
        this.ctx.moveTo(points[0].x, points[0].y);
        for (let i = 1; i < points.length; i++) this.ctx.lineTo(points[i].x, points[i].y);
      }
    }

    if (s.type !== 'line' && s.type !== 'freehand') this.ctx.fill();
    this.ctx.stroke();
  }

  // FIX: Corrected preview logic for all shapes
  drawPreview(x1: number, y1: number, x2: number, y2: number) {
    this.ctx.beginPath();
    this.ctx.strokeStyle = this.currentTool === 'freehand' ? this.currentColor : 'gray';
    this.ctx.lineWidth = 1;
    this.ctx.setLineDash(this.currentTool === 'freehand' ? [] : [5, 5]);

    if (this.currentTool === 'circle') {
        const r = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        this.ctx.arc(x1, y1, r, 0, 2 * Math.PI);
    }
    else if (this.currentTool === 'rectangle') {
        this.ctx.rect(x1, y1, x2 - x1, y2 - y1);
    }
    else if (this.currentTool === 'square') {
        const w = Math.abs(x2 - x1);
        const h = Math.abs(y2 - y1);
        const side = Math.max(w, h);
        const sqX = (x2 < x1) ? x1 - side : x1;
        const sqY = (y2 < y1) ? y1 - side : y1;
        this.ctx.rect(sqX, sqY, side, side);
    }
    else if (this.currentTool === 'line') {
        this.ctx.moveTo(x1, y1);
        this.ctx.lineTo(x2, y2);
    }
    else if (this.currentTool === 'ellipse') {
        const rx = Math.abs(x2 - x1) / 2;
        const ry = Math.abs(y2 - y1) / 2;
        const cx = Math.min(x1, x2) + rx;
        const cy = Math.min(y1, y2) + ry;
        this.ctx.ellipse(cx, cy, rx, ry, 0, 0, 2 * Math.PI);
    }
    else if (this.currentTool === 'triangle') {
        // Preview matches backend isosceles logic
        const w = Math.abs(x2 - x1);
        const h = Math.abs(y2 - y1);
        const topX = Math.min(x1, x2) + (w / 2.0);
        const topY = Math.min(y1, y2);

        const botLeftX = Math.min(x1, x2);
        const botLeftY = Math.min(y1, y2) + h;

        const botRightX = Math.min(x1, x2) + w;
        const botRightY = Math.min(y1, y2) + h;

        this.ctx.moveTo(topX, topY);
        this.ctx.lineTo(botLeftX, botLeftY);
        this.ctx.lineTo(botRightX, botRightY);
        this.ctx.closePath();
    }
    else if (this.currentTool === 'freehand') {
        if (this.freehandPoints.length > 0) {
            this.ctx.moveTo(this.freehandPoints[0].x, this.freehandPoints[0].y);
            for (let i = 1; i < this.freehandPoints.length; i++) this.ctx.lineTo(this.freehandPoints[i].x, this.freehandPoints[i].y);
        }
    }
    this.ctx.stroke();
    this.ctx.setLineDash([]);
  }

  drawSelectionOutline(shape: any) {
      const box = this.getBoundingBox(shape);
      this.ctx.save();
      this.ctx.strokeStyle = "#007bff";
      this.ctx.lineWidth = 1;
      this.ctx.setLineDash([6, 4]);
      this.ctx.strokeRect(box.x - 5, box.y - 5, box.width + 10, box.height + 10);

      this.ctx.fillStyle = "#ffffff"; this.ctx.setLineDash([]);
      const h = 8;
      this.ctx.fillRect(box.x + box.width + 5 - h/2, box.y + box.height + 5 - h/2, h, h);
      this.ctx.strokeRect(box.x + box.width + 5 - h/2, box.y + box.height + 5 - h/2, h, h);
      this.ctx.restore();
  }

  getHandleAt(px: number, py: number): string | null {
      if (!this.selectedShapeId) return null;
      const s = this.shapes.find(shape => shape.id === this.selectedShapeId);
      if (!s) return null;
      const box = this.getBoundingBox(s);
      const h = 12;
      const hx = box.x + box.width + 5; const hy = box.y + box.height + 5;
      if (px >= hx - h && px <= hx + h && py >= hy - h && py <= hy + h) return 'bottom-right';
      return null;
  }

  findShapeAt(px: number, py: number): Shape | null {
      for (let i = this.shapes.length - 1; i >= 0; i--) {
          const box = this.getBoundingBox(this.shapes[i]);
          if (px >= box.x - 5 && px <= box.x + box.width + 5 && py >= box.y - 5 && py <= box.y + box.height + 5) return this.shapes[i];
      }
      return null;
  }

  getBoundingBox(shape: any): {x:number, y:number, width:number, height:number} {
      switch(shape.type) {
          case 'rectangle': return { x: shape.x, y: shape.y, width: shape.width, height: shape.height };
          case 'square': return { x: shape.x, y: shape.y, width: shape.width, height: shape.height };
          case 'circle': return { x: shape.x - shape.radius, y: shape.y - shape.radius, width: shape.radius*2, height: shape.radius*2 };
          case 'ellipse': return { x: shape.x - shape.radiusX, y: shape.y - shape.radiusY, width: shape.radiusX*2, height: shape.radiusY*2 };
          case 'line': return { x: Math.min(shape.x, shape.x2), y: Math.min(shape.y, shape.y2), width: Math.abs(shape.x2 - shape.x), height: Math.abs(shape.y2 - shape.y) };
          case 'triangle':
              let tx = [shape.x, shape.x2, shape.x3], ty = [shape.y, shape.y2, shape.y3];
              return { x: Math.min(...tx), y: Math.min(...ty), width: Math.max(...tx) - Math.min(...tx), height: Math.max(...ty) - Math.min(...ty) };
          case 'freehand':
              if(!shape.points || shape.points.length === 0) return {x:0,y:0,width:0,height:0};
              let fx = shape.points.map((p:any)=>p.x), fy = shape.points.map((p:any)=>p.y);
              return { x: Math.min(...fx), y: Math.min(...fy), width: Math.max(...fx) - Math.min(...fx), height: Math.max(...fy) - Math.min(...fy) };
          default: return {x:0,y:0,width:0,height:0};
      }
  }
}
