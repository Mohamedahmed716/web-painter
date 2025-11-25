import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import {BoardComponent} from '../components/board/board';

@Injectable({
  providedIn: 'root'
})
export class DrawingService {
  private toolSource = new BehaviorSubject<string>('circle');
  private colorSource = new BehaviorSubject<string>('#000000');
  private board : BoardComponent = new BoardComponent();

  // Observables that the Board will subscribe to
  currentTool$ = this.toolSource.asObservable();
  currentColor$ = this.colorSource.asObservable();

  setTool(tool: string) {
    this.toolSource.next(tool);
  }

  setColor(color: string) {
    this.colorSource.next(color);
  }

  undo() {
    this.board.undo();
  }

  redo() {
    this.board.redo();
  }
}
