import { Injectable } from '@angular/core';
import {BehaviorSubject, Subject} from 'rxjs';
import {BoardComponent} from '../components/board/board';

@Injectable({
  providedIn: 'root'
})
export class DrawingService {
  private toolSource = new BehaviorSubject<string>('circle');
  private colorSource = new BehaviorSubject<string>('#000000');
  private undoSource = new Subject<any[]>();
  private redoSource = new Subject<any[]>();

  // Observables that the Board will subscribe to
  currentTool$ = this.toolSource.asObservable();
  currentColor$ = this.colorSource.asObservable();
  undo$ = this.undoSource.asObservable();
  redo$ = this.redoSource.asObservable();

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
}
