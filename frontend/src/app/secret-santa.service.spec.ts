import { TestBed } from '@angular/core/testing';

import { SecretSantaService } from './secret-santa.service';

describe('SecretSantaService', () => {
  let service: SecretSantaService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SecretSantaService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
