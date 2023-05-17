import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css'],
})
export class HomeComponent implements OnInit {
  constructor(private router: Router, private formBuilder: FormBuilder) {}

  redirectForm!: FormGroup;

  ngOnInit(): void {
    this.redirectForm = this.formBuilder.group({
      raffleId: [
        '',
        Validators.compose([
          Validators.required,
          Validators.maxLength(11),
          Validators.minLength(9),
        ]),
      ],
    });
  }

  redirect(): void {
    if (this.redirectForm.valid) {
      const code: string = new String(this.redirectForm.get('raffleId')?.value).toUpperCase();

      this.router.navigate([`raffle/existent/${code}`]);
    }
  }
}
