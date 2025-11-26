import { Injectable } from '@angular/core';
import { BehaviorSubject, Subject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class DrawingService {
  // State sources
  private toolSource = new BehaviorSubject<string>('circle');
  private colorSource = new BehaviorSubject<string>('#000000');

  // Triggers
  private undoSource = new Subject<void>();
  private redoSource = new Subject<void>();

  // Observables
  currentTool$ = this.toolSource.asObservable();
  currentColor$ = this.colorSource.asObservable();
  undo$ = this.undoSource.asObservable();
  redo$ = this.redoSource.asObservable();

  // Action Triggers
  delete$ = new Subject<void>();
  copy$ = new Subject<void>();

  // CRITICAL: This must carry the payload (List<Shape>) from the backend
  colorChange$ = new Subject<any[]>();

  triggerAction(action: string) {
    if (action === 'delete') this.delete$.next();
    if (action === 'copy') this.copy$.next();
  }

  setTool(tool: string) {
    this.toolSource.next(tool);
  }

  setColor(color: string) {
    this.colorSource.next(color);
  }

  undo() {
    this.undoSource.next();
  }
  redo() {
    this.redoSource.next();
  }
}
