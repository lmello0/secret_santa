import { AfterViewInit, Component, ElementRef, Input, OnInit, QueryList, ViewChild, ViewChildren } from '@angular/core';
import { FormBuilder, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { IRaffle } from '../raffle';
import { Router } from '@angular/router';
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
  
  @ViewChildren(ModalComponent) modalsRef!: QueryList<ModalComponent>;
  @ViewChild('adminCodeInput') adminCodeInputRef!: ElementRef;
  @ViewChild('copyCodeNotification') copyCoadToastRef!: ElementRef;
  @ViewChild('invalidBudgetNotification') invalidBudgetNotificationRef!: ElementRef;
  
  invalidBudgetToast: any;
  copyCodeToast: any;
  adminCodeInput: any;
  confirmCopyAdminCodeModal: any;

  showModal: any;

  copyButtonText: string = 'Mande para seus amigos!'

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
      this.adminForm.get('budget')?.setValue(this.raffle.budget);
    }
  }

  ngAfterViewInit(): void {
    this.confirmCopyAdminCodeModal = this.modalsRef.toArray()[1];
    this.adminCodeInput = this.adminCodeInputRef.nativeElement;
    this.copyCodeToast = this.copyCoadToastRef;
    this.invalidBudgetToast = this.invalidBudgetNotificationRef;
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

    this.copyCodeToast.toggle();
    setTimeout(() => {
      if (this.copyCodeToast.show){
        this.copyCodeToast.toggle();
      }
    }, 10000);

    this.copyBtnClicked = true;
  }

  copyRaffleCode() {
    if (!this.adminForm.get('budget')?.valid && this.currentUrl == '/raffle/new') {
      this.invalidBudgetToast.toggle();
      setTimeout(() => {
        if (this.invalidBudgetToast.show) {
          this.invalidBudgetToast.toggle();
        }
      }, 10000);
      return;
    }

    
    if (this.currentUrl == '/raffle/new') {
      this.raffle.budget = this.adminForm.get('budget')?.value;
      this.service.createRaffle(this.raffle).subscribe();
    }

    const url = window.location.href.replace('/new', `/existent/${this.raffle.code}`);
    navigator.clipboard.writeText(url);

    this.copyButtonText = 'Link copiado!'
    setTimeout(() => {
      this.copyButtonText = 'Mande para seus amigos!'
    }, 3000);
  }

  addParticipant() {
    if (!this.adminForm.get('budget')?.valid && this.currentUrl == '/raffle/new') {
      this.invalidBudgetToast.toggle();
      setTimeout(() => {
        if (this.invalidBudgetToast.show) {
          this.invalidBudgetToast.toggle();
        }
      }, 10000);
      return;
    }
    
    const participant: IParticipant = {
      id: this.raffle.participants.length + 1,
      name: this.addForm.get('name')?.value,
      email: this.addForm.get('email')?.value
    }
    
    if (this.currentUrl == '/raffle/new') {
      this.raffle.budget = this.adminForm.get('budget')?.value;
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
    const adminCode = this.adminForm.get('adminCode')?.value;
    this.adminCodeInput.classList.toggle('invalid-form', adminCode != this.raffle.adminCode);

    if (this.raffle.adminCode == adminCode) {
      this.service.startRaffle(this.raffle.code).subscribe(() => {
        window.location.reload();
      });
    }
  }

  showCopyModal() {
    if (!this.copyBtnClicked )
    if (!this.copyBtnClicked && (this.addForm.valid && (this.raffle.participants.length == 0 && this.addForm.valid))) {
      this.showModal = setTimeout(() => {
        this.confirmCopyAdminCodeModal.toggle();
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
