import { Component, Input, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { IRaffle } from '../raffle';
import { ActivatedRoute, Router } from '@angular/router';
import { SecretSantaService } from 'src/app/secret-santa.service';

@Component({
  selector: 'app-menu',
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.css'],
})
export class MenuComponent implements OnInit {
  constructor(
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private service: SecretSantaService
  ) {}

  addForm!: FormGroup;
  adminForm!: FormGroup;
  controlForm!: FormGroup;

  @Input() raffle: IRaffle = {} as IRaffle;
  @Input() canSave: Boolean = false;
  copyBtnClicked: Boolean = false;

  currentUrl: String = this.router.url;

  isCopyMessageHidden: Boolean = true;
  savedBtnClass = {
    class: 'fa-solid fa-save',
    status: false,
    text: ''
  };

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
      adminCode: ['', Validators.required],
    });

    let fromNew = this.route.snapshot.queryParams['fromNew'];
    if (this.currentUrl == '/raffle/new' || fromNew === 'true') {
      this.adminForm.get('adminCode')?.setValue(this.raffle.adminCode);
    }
  }

  sendData() {
    this.raffle.participants.push({
      id: this.raffle.participants.length + 1,
      name: this.addForm.get('name')?.value,
      email: this.addForm.get('email')?.value,
    });

    this.canSave = true;
  }

  allowButtonAddPanel(): string {
    return this.addForm.valid ? 'btn' : 'btn btn-disabled';
  }

  allowButtonControlPanel(): string {
    return this.adminForm.valid && this.raffle.participants.length >= 4 ? 'btn' : 'btn btn-disabled';
  }

  allowSaveButton(): string {
    return this.canSave ? 'btn' : 'btn btn-disabled';
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

  changeSaveBtn(): void {
    this.savedBtnClass.class = 'fa-solid fa-check';
    this.savedBtnClass.text = 'Salvo!'

    setTimeout(() => {
      this.savedBtnClass.class = 'fa-solid fa-save';
      this.savedBtnClass.text = '';
    }, 5000);
  }

  saveData(): void {
    this.savedBtnClass.status = true;
    this.changeSaveBtn();

    this.service.saveRaffle(this.raffle).subscribe(() => {
      if (this.currentUrl == '/raffle/new') {
        this.router.navigate([`/raffle/existent/${this.raffle.code}`]);
      }
    });

    this.canSave = false;
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
}
