import { AfterViewInit, Component, ElementRef, Input, OnInit, QueryList, ViewChild, ViewChildren } from '@angular/core';
import { FormBuilder, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { IRaffle } from '../raffle';
import { ActivatedRoute, Router } from '@angular/router';
import { SecretSantaService } from 'src/app/secret-santa.service';
import { IParticipant } from '../participant';
import { ModalComponent } from 'src/app/modal/modal.component';

@Component({
  selector: 'app-menu',
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.css'],
})
export class MenuComponent implements OnInit, AfterViewInit {
  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private service: SecretSantaService
  ) {}

  addForm!: FormGroup;
  adminForm!: FormGroup;
  controlForm!: FormGroup;

  @Input() raffle: IRaffle = {} as IRaffle;
  copyBtnClicked: Boolean = false;
  
  currentUrl: string = this.router.url;
  
  isCopyMessageHidden: Boolean = true;
  
  @ViewChildren(ModalComponent) modals!: QueryList<ModalComponent>;

  confirmCopyAdminCode: any;
  showModal: any;

  ngOnInit(): void {
    this.addForm = this.formBuilder.group({
      name: [
        '',
        Validators.compose([
          Validators.required,
          Validators.pattern(/[a-zA-Z\ \`]+/),
        ]),
      ],
      email: ['', Validators.compose([Validators.required, Validators.email])],
    });

    this.adminForm = this.formBuilder.group({
      budget: ['', Validators.compose([Validators.required, Validators.min(0)])],
      adminCode: ['', Validators.required],
    });

    if (this.currentUrl == '/raffle/new') {
      this.adminForm.get('adminCode')?.setValue(this.raffle.adminCode);
    } else {
      this.adminForm.get('budget')?.disable();
    }
  }

  ngAfterViewInit(): void {
    this.confirmCopyAdminCode = this.modals.last;
  }

  allowButtonAddPanel(): string {
    return this.addForm.valid ? 'btn' : 'btn btn-disabled';
  }

  allowButtonControlPanel(): string {
    return this.adminForm.valid && this.raffle.participants.length >= 4 ? 'btn' : 'btn btn-disabled';
  }

  copyAdminCode() {
    const adminCode = this.adminForm.get('adminCode')?.value;
    navigator.clipboard.writeText(adminCode);

    this.isCopyMessageHidden = !this.isCopyMessageHidden;
    setTimeout(() => {
      this.isCopyMessageHidden = !this.isCopyMessageHidden;
    }, 5000);

    this.copyBtnClicked = true;
  }

  addParticipant() {
    const participant: IParticipant = {
      id: this.raffle.participants.length + 1,
      name: this.addForm.get('name')?.value,
      email: this.addForm.get('email')?.value
    }
    
    if (this.currentUrl == '/raffle/new') {
      this.raffle.participants.push(participant);

      this.service.createRaffle(this.raffle)
        .subscribe(() => {
          this.router.navigate([`/raffle/existent/${this.raffle.code}`]);
        });
    } else {
      this.service.addParticipant(this.raffle.code, participant).subscribe();
      this.raffle.participants.push(participant)
    }

    this.addForm.reset();
  }

  deleteRaffle() {
    if (this.currentUrl == '/raffle/new') {
      this.router.navigate(['/home']);
    } else {
      this.service.deleteRaffle(this.raffle.code).subscribe(() => {
        this.router.navigate(['/home']);
      });
    }
  }

  startSecretSanta() {
    console.log(this.raffle);
  }

  showCopyModal() {
    if (!this.copyBtnClicked )
    if (!this.copyBtnClicked && (this.addForm.valid && (this.raffle.participants.length == 0 && this.addForm.valid))) {
      this.showModal = setTimeout(() => {
        this.confirmCopyAdminCode.toggle();
      }, 300);
    }
  }

  leaveModal() {
    clearTimeout(this.showModal);
  }

  formatBudget(event: any) {
    const value = event.target.value.replace(/[^0-9\.\,]/g, '');

    this.adminForm.get('budget')?.setValue(value);
  }

  validateForm(errors: ValidationErrors | null | undefined, touched: boolean | undefined): string {
    return errors && touched ? 'invalid-form' : '';
  }
}
