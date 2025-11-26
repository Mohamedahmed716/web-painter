import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Shape } from '../models/shape';
import { Parsing } from './parsing';

@Injectable({
  providedIn: 'root',
})
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
      .pipe(map((data) => this.parsing.parse(data)));
  }

  redo(): Observable<Shape[]> {
    return this.http
      .post<any[]>(`${this.baseUrl}/redo`, {})
      .pipe(map((data) => this.parsing.parse(data)));
  }
}
