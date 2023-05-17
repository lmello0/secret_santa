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
    private service: SecretSantaService,
  ) {
    console.log(String(this.router.getCurrentNavigation()?.extras.state).toString());
  }

  addForm!: FormGroup;
  adminForm!: FormGroup;
  controlForm!: FormGroup;

  @Input() raffle: IRaffle = {} as IRaffle;
  @Input() canSave: Boolean = false;

  currentUrl: String = this.router.url;

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
    return this.adminForm.valid ? 'btn' : 'btn btn-disabled';
  }

  allowSaveButton(): string {
    return this.canSave ? 'btn' : 'btn btn-disabled';
  }

  saveData(): void {
    this.service.saveRaffle(this.raffle).subscribe(() => {
      if (this.currentUrl == '/raffle/new') {
        this.router.navigate([`/raffle/existent/${this.raffle.code}`], {
          queryParams: { fromNew: true },
          state: { adminCode: this.raffle.adminCode }
        });
      } else {
        this.router.navigate([this.currentUrl]);
      }
    });

    this.canSave = false;
  }

  deleteRaffle() {
    this.service.deleteRaffle(this.raffle.code).subscribe(() => {
      this.router.navigate(['/home']);
    });
  }
}
