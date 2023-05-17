import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { IRaffle } from './raffle/raffle';

@Injectable({
  providedIn: 'root'
})
export class SecretSantaService {
  private readonly API = 'http://localhost:3000';

  constructor(private http: HttpClient) { }

  getRaffle(raffleCode: string): Observable<IRaffle> {
    const url: string = `${this.API}/getRaffle/${raffleCode}`;

    return this.http.get<IRaffle>(url);
  }

  saveRaffle(raffle: IRaffle): Observable<IRaffle> {
    const url: string = `${this.API}/updateRaffle`;

    return this.http.put<IRaffle>(url, raffle);
  }

  deleteRaffle(code: string): Observable<IRaffle> {
    const url : string = `${this.API}/deleteRaffle/${code}`;

    return this.http.delete<IRaffle>(url);
  }
}
