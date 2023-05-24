import { animate, style, transition, trigger } from '@angular/animations';
import { Component } from '@angular/core';

@Component({
  selector: 'app-toast-notification',
  templateUrl: './toast-notification.component.html',
  styleUrls: ['./toast-notification.component.css'],
  animations: [
    trigger('enter', [
      transition(':enter', [
        style({ left: '-20%' }),
        animate('100ms', style({ left: '1%' })),
      ]),
      transition(':leave', [
        animate('500ms', style({ left: '-20%' }))
      ])
    ]),
  ]
})
export class ToastNotificationComponent {
  show: boolean = false;

  toggle() {
    this.show = !this.show;
  }
}
