export interface Shape {
  type: string;
  id?: string;
  x: number;
  y: number;
  color: string;
  fillColor?: string;
  strokeWidth?: number;
  radius?: number;
  width?: number;
  height?: number;
  radiusX?: number;
  radiusY?: number;
  selected?: boolean;
}
