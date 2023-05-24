import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css'],
})
export class NavbarComponent {
  constructor(private router: Router, private formBuilder: FormBuilder) {
    this.searchForm = this.formBuilder.group({
      search: [
        '',
        Validators.compose([
          Validators.required,
          Validators.maxLength(11),
          Validators.minLength(9),
        ]),
      ],
    });

    if (this.router.url == '/about') {
      this.linkInfo = {
        title: 'Novo amigo secreto',
        url: '/raffle/new',
      };
    } else {
      this.linkInfo = {
        title: 'Sobre',
        url: '/about',
      };
    }
  }

  linkInfo = {
    title: '',
    url: '',
  };

  searchForm!: FormGroup;

  redirect(): void {
    if (this.searchForm.valid) {
      const code: string = new String(this.searchForm.get('search')?.value).toUpperCase();

      this.router.navigate([`raffle/existent/${code}`]);
    }
  }
}
