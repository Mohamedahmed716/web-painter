import { Component, inject, ElementRef, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DrawingService } from '../../services/drawing';

@Component({
  selector: 'app-toolbar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './toolbar.html',
  styleUrls: ['./toolbar.css'],
})
export class ToolbarComponent {
  private drawingService = inject(DrawingService);
  private elementRef = inject(ElementRef);

  // Added 'freehand' tool to the list
  tools = [
    { id: 'freehand', icon: '✎', label: 'Freehand' }, // The new cursor tool
    { id: 'line', icon: '╱', label: 'Line' },
    { id: 'circle', icon: '○', label: 'Circle' },
    { id: 'rectangle', icon: '▭', label: 'Rectangle' },
    { id: 'square', icon: '□', label: 'Square' },
    { id: 'triangle', icon: '△', label: 'Triangle' },
    { id: 'ellipse', icon: '⬭', label: 'Ellipse' },
  ];

  colors: string[] = [
    '#000000',
    '#FF0000',
    '#00FF00',
    '#0000FF',
    '#FFFF00',
    '#FFA500',
    '#800080',
    '#FFFFFF',
  ];

  activeTool: string = 'line';
  activeColor: string = '#000000';
  strokeColor: string = '#333333';
  fillColor: string = '#ffffff';
  strokeWidth: number = 2;

  // State
  isDropdownOpen = false;

  selectTool(toolId: string) {
    this.activeTool = toolId;
    this.drawingService.setTool(toolId);
  }

  // --- Properties Logic ---
  onStrokeColorChange() {
    this.activeColor = this.strokeColor;
    this.drawingService.setColor(this.strokeColor);
  }

  onFillColorChange() {
    // Implement fill logic in service if needed
    console.log('Fill color changed:', this.fillColor);
  }

  onPropertyChange(prop: string, value: any) {
    console.log('Property changed:', prop, value);
    // You can extend DrawingService to handle width
  }

  // --- Actions ---
  undo() {
    this.drawingService.undo();
  }

  redo() {
    this.drawingService.redo();
  }

  triggerAction(action: string) {
    console.log('Action triggered:', action);
  }

  // --- Dropdown Logic ---
  toggleDropdown() {
    this.isDropdownOpen = !this.isDropdownOpen;
  }

  @HostListener('document:click', ['$event'])
  onClickOutside(event: MouseEvent) {
    if (!this.isDropdownOpen) return;
    const clickedInside = this.elementRef.nativeElement.contains(event.target);
    if (!clickedInside) {
      this.isDropdownOpen = false;
    }
  }
  onSaveClick(format: 'json' | 'xml') {
    this.isDropdownOpen = false;
    this.save(format);
  }

  async save(format: 'json' | 'xml') {
    this.drawingService.saveFile(format).subscribe({
      next: async (blob: Blob) => {
        // --- الخطة (أ): المحاولة باستخدام الطريقة الحديثة ---
        try {
          // @ts-ignore: عشان التايب سكريبت ميعترضش على الميزة الجديدة دي
          if (window.showSaveFilePicker) {
            // 1. اطلب من المستخدم يختار مكان واسم الملف
            // @ts-ignore
            const handle = await window.showSaveFilePicker({
              suggestedName: `drawing.${format}`,
              types: [
                {
                  description: `${format.toUpperCase()} File`,
                  accept: { [format === 'json' ? 'application/json' : 'text/xml']: [`.${format}`] },
                },
              ],
            });

            // 2. اكتب الداتا جوه الملف اللي اختاره
            const writable = await handle.createWritable();
            await writable.write(blob);
            await writable.close();

            alert('File saved successfully!');
            return; // اخرج من الدالة لو نجحنا (عشان منروحش للخطة ب)
          }
        } catch (err: any) {
          // لو المستخدم داس Cancel في النافذة، مفيش داعي نكمل
          if (err.name === 'AbortError') return;
          console.warn('File System Access API not supported or failed, falling back to download.');
        }

        // --- الخطة (ب): الطريقة القديمة (Fallback) ---
        // دي هتشتغل لو المتصفح قديم أو مش بيدعم الخاصية دي (زي Firefox)
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `drawing.${format}`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => alert('Error fetching file from server'),
    });
  }

  onFileSelected(event: any) {
    const file: File = event.target.files[0];

    if (file) {
      this.drawingService.loadFile(file);
    }

    event.target.value = '';
  }
}
