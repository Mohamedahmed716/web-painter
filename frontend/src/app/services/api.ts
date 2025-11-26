import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Shape } from '../models/shape';
import { Parsing } from './parsing';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private http = inject(HttpClient);
  private parsing = inject(Parsing);
  private baseUrl = 'http://localhost:8080/api';

  getShapes(): Observable<Shape[]> {
    return this.http
      .get<any[]>(`${this.baseUrl}/shapes`)
      .pipe(map((data) => this.parsing.parse(data)));
  }

  createShape(type: string, params: any): Observable<Shape[]> {
    return this.http
      .post<any[]>(`${this.baseUrl}/create`, { type, params })
      .pipe(map((data) => this.parsing.parse(data)));
  }

  undo(): Observable<Shape[]> {
    return this.http
      .post<any[]>(`${this.baseUrl}/undo`, {})
      .pipe(map((d) => this.parsing.parse(d)));
  }

  redo(): Observable<Shape[]> {
    return this.http
      .post<any[]>(`${this.baseUrl}/redo`, {})
      .pipe(map((d) => this.parsing.parse(d)));
  }

  select(x: number, y: number): Observable<Shape[]> {
    return this.http
      .post<any[]>(`${this.baseUrl}/select`, { x, y })
      .pipe(map((d) => this.parsing.parse(d)));
  }

  startMove() {
    return this.http.post<any[]>(`${this.baseUrl}/move/start`, {});
  }

  moveSelected(dx: number, dy: number): Observable<Shape[]> {
    return this.http
      .post<any[]>(`${this.baseUrl}/move`, { dx, dy })
      .pipe(map((d) => this.parsing.parse(d)));
  }

  endMove() {
    return this.http
      .post<any[]>(`${this.baseUrl}/move/end`, {})
      .pipe(map((d) => this.parsing.parse(d)));
  }

  resizeSelected(anchor: string, dx: number, dy: number): Observable<Shape[]> {
    return this.http
      .post<any[]>(`${this.baseUrl}/resize`, { anchor, dx, dy })
      .pipe(map((d) => this.parsing.parse(d)));
  }

  copySelected(): Observable<Shape[]> {
    return this.http
      .post<any[]>(`${this.baseUrl}/copy`, {})
      .pipe(map((d) => this.parsing.parse(d)));
  }

  deleteSelected(): Observable<Shape[]> {
    return this.http
      .post<any[]>(`${this.baseUrl}/delete`, {})
      .pipe(map((d) => this.parsing.parse(d)));
  }

  updateColor(color: string): Observable<Shape[]> {
    return this.http
      .post<any[]>(`${this.baseUrl}/color`, { color })
      .pipe(map((d) => this.parsing.parse(d)));
  }

  // NEW: Returns Blob for "Save As"
  downloadFile(format: string): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/save/${format}`, { responseType: 'blob' });
  }

  load(file: File) {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(`${this.baseUrl}/load`, formData, { responseType: 'text' });
  }

  pasteSelected(x: number, y: number): Observable<Shape[]> {
    return this.http
      .post<any[]>(`${this.baseUrl}/paste`, { x, y })
      .pipe(map((d) => this.parsing.parse(d)));
  }
  // NEW: Update Fill Color
  updateFillColor(color: string): Observable<Shape[]> {
    return this.http
      .post<any[]>(`${this.baseUrl}/fill`, { color })
      .pipe(map((d) => this.parsing.parse(d)));
  }
}
