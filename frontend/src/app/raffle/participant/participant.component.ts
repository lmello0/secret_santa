import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { IParticipant } from '../participant';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-participant',
  templateUrl: './participant.component.html',
  styleUrls: ['./participant.component.css'],
})
export class ParticipantComponent implements OnInit {
  @Input() participant: IParticipant = {} as IParticipant;
  @Output() participantChange = new EventEmitter<IParticipant>();

  @Output() destroy = new EventEmitter<number>();
  edit: boolean = false;
  editForm!: FormGroup;

  constructor(private formBuilder: FormBuilder) { }

  ngOnInit(): void {
    this.editForm = this.formBuilder.group({
      name: [this.participant.name, Validators.compose([Validators.required, Validators.pattern(/[a-zA-Z\ \`]+/)])],
      email: [this.participant.email, Validators.compose([Validators.required, Validators.email])]
    });
  }

  onDelete(): void {
    this.destroy.emit(this.participant.id);
  }

  onEdit(): void {
    this.edit = !this.edit;
  }

  allowEditButton(): string {
    return this.editForm.valid ? 'card-button card-edit-button' : 'card-button card-button-disabled';
  }

  confirmChanges(): void {
    this.participant = {
      id: this.participant.id,
      name: this.editForm.get('name')?.value,
      email: this.editForm.get('email')?.value
    }

    this.edit = !this.edit;
    this.participantChange.emit(this.participant);
  }
}
