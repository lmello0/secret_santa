import { Component, EventEmitter, Input, Output } from '@angular/core';
import { IParticipant } from '../participant';
import { SecretSantaService } from 'src/app/secret-santa.service';
import { IRaffle } from '../raffle';

@Component({
  selector: 'app-participants-list',
  templateUrl: './participants-list.component.html',
  styleUrls: ['./participants-list.component.css']
})
export class ParticipantsListComponent {
  @Input() raffle: IRaffle = {} as IRaffle;

  constructor(private service: SecretSantaService) { }

  onDeleteParticipant(event: number) {
    event = event - 1;

    this.service.saveRaffle(this.raffle)
      .subscribe(() => {
        this.raffle.participants.splice(event, 1);

        for (let i = 0; i < this.raffle.participants.length; i++) {
          this.raffle.participants[i].id = i + 1;
        }
      });
  }

  onParticipantChange(participant: IParticipant) {
    participant.id = participant.id! - 1;

    this.raffle.participants[participant.id!] = participant;
    this.raffle.participants[participant.id!].id = participant.id! + 1;

    this.service.saveRaffle(this.raffle).subscribe();
  }

  canDeleteParticipant(): boolean {
    return this.raffle.participants.length > 1 ? true : false;
  }
}
