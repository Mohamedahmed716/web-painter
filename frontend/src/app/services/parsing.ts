import { Injectable } from '@angular/core';
import { Shape } from '../models/shape';

@Injectable({
  providedIn: 'root',
})
export class Parsing {
  parse(rawJson: any[]): Shape[] {
    if (!rawJson || !Array.isArray(rawJson)) {
      return [];
    }
    return rawJson.map((item) => this.convertSingleShape(item));
  }

  private convertSingleShape(item: any): Shape {
    const shape: Shape = {
      type: item.type,
      id: item.id,
      x: item.x,
      y: item.y,
      color: item.color,

      // *** CRITICAL: MAP FILL COLOR ***
      fillColor: item.fillColor,

      radius: undefined,
      width: undefined,
      height: undefined,
    };

    switch (item.type) {
      case 'circle':
        shape.radius = item.radius;
        break;
      case 'rectangle':
        shape.width = item.width;
        shape.height = item.height;
        break;
      case 'square':
        shape.width = item.sideLength;
        shape.height = item.sideLength;
        break;
      case 'ellipse':
        shape.radiusX = item.radiusX;
        shape.radiusY = item.radiusY;
        break;
      case 'line':
        (shape as any).x2 = item.x2;
        (shape as any).y2 = item.y2;
        break;
      case 'triangle':
        (shape as any).x2 = item.x2;
        (shape as any).y2 = item.y2;
        (shape as any).x3 = item.x3;
        (shape as any).y3 = item.y3;
        break;
      case 'freehand':
        (shape as any).points = item.points;
        break;
    }
    return shape;
  }
}
