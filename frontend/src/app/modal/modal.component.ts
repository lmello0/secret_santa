import { Component, NgModule } from '@angular/core';

@Component({
  selector: 'app-modal',
  templateUrl: './modal.component.html',
  styleUrls: ['./modal.component.css']
})
export class ModalComponent {
  show: boolean = false;

  toggle() {
    this.show = !this.show;
  }
}
