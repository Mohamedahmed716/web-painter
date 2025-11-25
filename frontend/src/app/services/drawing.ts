import { Injectable,inject } from '@angular/core';
import {BehaviorSubject, Subject} from 'rxjs';
import {BoardComponent} from '../components/board/board';
import { ApiService } from './api';

@Injectable({
  providedIn: 'root'
})
export class DrawingService {
  private apiService = inject(ApiService);

  private toolSource = new BehaviorSubject<string>('circle');
  private colorSource = new BehaviorSubject<string>('#000000');
  private undoSource = new Subject<any[]>();
  private redoSource = new Subject<any[]>();
  private shapesSource = new Subject<any[]>();

  // Observables that the Board will subscribe to
  currentTool$ = this.toolSource.asObservable();
  currentColor$ = this.colorSource.asObservable();
  undo$ = this.undoSource.asObservable();
  redo$ = this.redoSource.asObservable();
  shapes$ = this.shapesSource.asObservable();

  setTool(tool: string) {
    this.toolSource.next(tool);
  }

  setColor(color: string) {
    this.colorSource.next(color);
  }

  undo() {
    this.undoSource.next([]);
  }

  redo() {
    this.redoSource.next([])
  }

  saveFile(format: 'json' | 'xml') {
    return this.apiService.downloadFile(format);
  }

  loadFile(file: File) {
    this.apiService.uploadFile(file).subscribe({
      next: (response) => {
        console.log("Upload response:", response);
        this.refreshBoard();
      },
      error: (err) => console.error('Upload failed', err)
    });
  }

  refreshBoard() {
    this.apiService.getShapes().subscribe(shapes => {
      this.shapesSource.next(shapes);
    });
  }
}
