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

  getRaffle(raffleCode: String): Observable<IRaffle> {
    const url = `${this.API}/getRaffle/${raffleCode}`;

    return this.http.get<IRaffle>(url);
  }

  saveRaffle(raffle: IRaffle): Observable<IRaffle> {
    const url = `${this.API}/updateRaffle`;

    return this.http.put<IRaffle>(url, raffle);
  }

  deleteRaffle(code: String): Observable<IRaffle> {
    const url = `${this.API}/deleteRaffle/${code}`;

    return this.http.delete<IRaffle>(url);
  }

  startRaffle(code: String): Observable<IRaffle> {
    const url = `${this.API}/startRaffle`;

    return this.http.post<IRaffle>(url, code);
  }
}
