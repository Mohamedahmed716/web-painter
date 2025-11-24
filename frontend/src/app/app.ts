import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { BoardComponent } from './components/board/board';
import { ToolbarComponent } from './components/toolbar/toolbar';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, BoardComponent, ToolbarComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('frontend');
}
