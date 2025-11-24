import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';


import { BoardComponent } from './components/board/board';
import { Toolbar } from './components/toolbar/toolbar';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, BoardComponent, Toolbar],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('frontend');
}
