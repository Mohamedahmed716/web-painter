import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Shape } from '../models/shape';


@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8080/api';

  getShapes(): Observable<Shape[]> {
    return this.http.get<Shape[]>(`${this.baseUrl}/shapes`);
  }

  createShape(type: string, params: any): Observable<Shape[]> {
    return this.http.post<Shape[]>(`${this.baseUrl}/create`, { type, params });
  }

  // add undo/redo/save/load here
  undo(): Observable<Shape[]> {
    return this.http.post<Shape[]>(`${this.baseUrl}/undo`, {});
  }

  redo(): Observable<Shape[]> {
    return this.http.post<Shape[]>(`${this.baseUrl}/redo`, {});
  }
}
