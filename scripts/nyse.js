//async function main() {
//    const resp = await fetch("https://www.nyse.com/api/quotes/filter", {
//    "headers": {
//        "accept": "*/*",
//        "accept-language": "en-US,en;q=0.9",
//        "content-type": "application/json",
//        "sec-ch-ua": "\"Not A(Brand\";v=\"99\", \"Google Chrome\";v=\"121\", \"Chromium\";v=\"121\"",
//        "sec-ch-ua-mobile": "?0",
//        "sec-ch-ua-platform": "\"macOS\"",
//        "sec-fetch-dest": "empty",
//        "sec-fetch-mode": "cors",
//        "sec-fetch-site": "same-origin",
//        "Referer": "https://www.nyse.com/listings_directory/stock",
//        "Referrer-Policy": "strict-origin-when-cross-origin"
//    },
//    "body": "{\"instrumentType\":\"EQUITY\",\"pageNumber\":1,\"sortColumn\":\"NORMALIZED_TICKER\",\"sortOrder\":\"ASC\",\"maxResultsPerPage\":7200,\"filterToken\":\"\"}",
//    "method": "POST"
//    });
//    const json = await resp.json();
//    console.log(JSON.stringify(json));
//
//}

async function main() {
    const resp = await fetch("https://api.nasdaq.com/api/screener/stocks?tableonly=true&limit=4200&exchange=NYSE", {
    "headers": {
        "accept": "application/json, text/plain, */*",
        "accept-language": "en-US,en;q=0.9",
        "sec-ch-ua": "\"Not A(Brand\";v=\"99\", \"Google Chrome\";v=\"121\", \"Chromium\";v=\"121\"",
        "sec-ch-ua-mobile": "?0",
        "sec-ch-ua-platform": "\"macOS\"",
        "sec-fetch-dest": "empty",
        "sec-fetch-mode": "cors",
        "sec-fetch-site": "same-site",
        "Referer": "https://www.nasdaq.com/",
        "Referrer-Policy": "strict-origin-when-cross-origin"
    },
    "body": null,
    "method": "GET"
    });
    const json = await resp.json();
    console.log(JSON.stringify(json));
}

main();