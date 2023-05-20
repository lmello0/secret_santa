import { AfterViewInit, Component, Input, OnInit, ViewChild } from '@angular/core';
import { SecretSantaService } from '../secret-santa.service';
import { ActivatedRoute, Router } from '@angular/router';
import { IRaffle } from './raffle';
import { ModalComponent } from '../modal/modal.component';

@Component({
  selector: 'app-raffle',
  templateUrl: './raffle.component.html',
  styleUrls: ['./raffle.component.css'],
})
export class RaffleComponent implements OnInit, AfterViewInit {
  constructor(
    private service: SecretSantaService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  @Input() raffle: IRaffle = {} as IRaffle;
  
  @ViewChild(ModalComponent) modal!: ModalComponent;
  raffleAlreadyStarted: any;

  private chars: string[] = [
    ...'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890!@#$%&*',
  ];

  ngOnInit(): void {
    const code = this.route.snapshot.paramMap.get('code');

    if (code != null) {
      this.service.getRaffle(code!)
        .subscribe((data) => {
          if (!data) {
            this.router.navigate(['/raffle/new']);
          }

          this.raffle = data;

          if (this.raffle.started) {
            this.raffleAlreadyStarted.toggle();
          }

          for (let i = 0; i < this.raffle.participants.length; i++) {
            this.raffle.participants[i].id = i + 1;
          }
        }
      );
    } else {
      this.raffle = {
        code: this.generateCode(),
        adminCode: this.generateAdminCode(),
        budget: 0,
        participants: [],
        started: false,
        version: 0,
      };
    }
  }

  ngAfterViewInit(): void {
    this.raffleAlreadyStarted = this.modal;
  }

  generateAdminCode(): string {
    let code: string = '';

    for (let i = 0; i < 12; i++) {
      code = code + this.chars[Math.floor(Math.random() * this.chars.length)];
    }

    return code;
  }

  generateCode(): string {
    let code: string = '';

    for (let i = 0; i < 3; i++) {
      for (let j = 0; j < 3; j++) {
        code = code + this.chars[Math.floor(Math.random() * 26)];
      }
      code = code + '-';
    }

    code = code.slice(0, 11);
    return code;
  }
}

