import { IParticipant } from "./database";
import { createTransport } from "nodemailer";
import { hashSync, genSaltSync } from "bcryptjs";
import * as path from 'path';

const hbs = require('nodemailer-express-handlebars');
interface IDraft {
    sender: IParticipant,
    recipient: IParticipant
}

const transporter = createTransport({
    host: process.env.EMAIL_HOST,
    port: Number(process.env.EMAIL_PORT),
    secure: true,
    auth: {
        user: process.env.EMAIL_USER,
        pass: process.env.EMAIL_PASS
    },
    tls: {
        rejectUnauthorized: false
    }
});

const handlebarOptions = {
    viewEngine: {
        partialsDir: path.resolve('./email/'),
        defaultLayout: false,
    },
    viewPath: path.resolve('./email/')
};

transporter.use('compile', hbs(handlebarOptions));

export function draft(participants: IParticipant[]): IDraft[] {
    let recipients: IParticipant[] = [];

    let cont: number = 0;
    while(cont == 0) {
        let redo: boolean = false;
        let possibleSanta: IParticipant[] = participants.slice();

        const draftLength: number = possibleSanta.length;

        for(let i: number = 0; i < draftLength; i++) {
            let recipIdx: number = Math.floor(Math.random() * possibleSanta.length);

            let x: number = 0;
            while(x == 0) {
                if (participants[i] == possibleSanta[recipIdx]) {
                    if (possibleSanta.length == 1) {
                        redo = true;
                        x = 1;
                    } else {
                        recipIdx = Math.floor(Math.random() * possibleSanta.length);
                    }
                } else {
                    x = 1;
                }
            }

            if (!redo) {
                recipients.push(possibleSanta[recipIdx]);
                possibleSanta.splice(recipIdx, 1);
                cont = 1;
            } else {
                cont = 0;
            }
        }
    }

    let returnData: IDraft[] = [];
    participants.forEach((participant, index) => {
        returnData.push({
            sender: { name: participant.name, email: participant.email },
            recipient: { name: recipients[index].name, email: recipients[index].email }
        });
    });

    return returnData;
}

export function send(draft: IDraft, code: string, budget: string): void {
    const mailData = {
        subject: `Pessoa sorteada: [${code}]`,
        to: draft.sender.email,
        template: 'email',
        context: {
            code: code,
            sender: draft.sender.name,
            recipient: draft.recipient.name,
            budget: `R$${budget},00`
        }
    }

    transporter.sendMail(mailData);
}

export function hashString(stringToHash: string) {
    const salt = genSaltSync(10);
    const hash = hashSync(stringToHash, salt);

    return hash;
}