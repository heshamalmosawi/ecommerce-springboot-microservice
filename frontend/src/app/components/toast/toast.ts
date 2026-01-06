import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-toast',
  imports: [CommonModule],
  templateUrl: './toast.html',
  styleUrl: './toast.scss'
})
export class Toast {
  message: string = '';
  type: 'success' | 'error' | 'info' = 'success';
  visible: boolean = false;
  private timeoutId: any;

  show(message: string, type: 'success' | 'error' | 'info' = 'success', duration: number = 3000) {
    this.message = message;
    this.type = type;
    this.visible = true;

    if (this.timeoutId) {
      clearTimeout(this.timeoutId);
    }

    this.timeoutId = setTimeout(() => {
      this.hide();
    }, duration);
  }

  hide() {
    this.visible = false;
  }
}
