import { Component, EventEmitter, Input, Output } from '@angular/core';
import { IParticipant } from '../participant';

@Component({
  selector: 'app-participants-list',
  templateUrl: './participants-list.component.html',
  styleUrls: ['./participants-list.component.css']
})
export class ParticipantsListComponent {
  @Input() participants: IParticipant[] = [];

  @Output() canSave = new EventEmitter<Boolean>();

  onDeleteParticipant(event: number) {
    for(let i = 0; i < this.participants.length; i++) {
      if (this.participants[i].id == event) {
        this.participants.splice(i, 1);
      }
      
      this.participants[i].id = i + 1;
    }
  }

  onParticipantChange() {
    this.canSave.emit(true);
  }
}
