<?php

require_once __DIR__ . '/vendor/autoload.php';

const OS_AUTH_URL = "https://id.cloud.acvile.com/v3/";
const OS_REGION = "eu-east-1";

// follow the documentation to get an application credential for using the Openstack APIs
// https://help.cloudify.ro/en/article/generate-application-credentials-for-project-1wlq1za/
const OS_CREDENTIAL_ID = '{your_application_credential_id}';
const OS_CREDENTIAL_SECRET = '{your_application_credential_secret}';

/**
 * Get the token using application_credentials mechanism
 * This is not implemented yet in php-opencloud/openstack
 */

use GuzzleHttp\Client;
use GuzzleHttp\Middleware as GuzzleMiddleware;
use OpenStack\Common\Transport\HandlerStack;
use OpenStack\Common\Transport\Utils;
use OpenStack\Identity\v3\Models\Token;
use OpenStack\Identity\v3\Service;

function get_client(array $options): Client
{
    if (!isset($options['authUrl'])) {
        throw new \InvalidArgumentException("'authUrl' is a required option");
    }

    $stack = HandlerStack::create();

    if (!empty($options['debugLog'])
        && !empty($options['logger'])
        && !empty($options['messageFormatter'])
    ) {
        $logMiddleware = GuzzleMiddleware::log($options['logger'], $options['messageFormatter']);
        $stack->push($logMiddleware, 'logger');
    }

    $clientOptions = [
        'base_uri' => Utils::normalizeUrl($options['authUrl']),
        'handler' => $stack,
    ];

    if (isset($options['requestOptions'])) {
        $clientOptions = array_merge($options['requestOptions'], $clientOptions);
    }

    return new Client($clientOptions);
}

class AppCredentialsIdentityService extends Service
{
    public function generateToken(array $options): Token
    {
        $client = new Client([
            'base_uri' => OS_AUTH_URL
        ]);

        $resp = $client->post('auth/tokens', [
            'json' => [
                // body reference:
                // https://docs.openstack.org/api-ref/identity/v3/index.html?expanded=authenticating-with-an-application-credential-detail#authenticating-with-an-application-credential
                'auth' => [
                    'identity' => [
                        'methods' => ['application_credential'],
                        'application_credential' => [
                            'id' => OS_CREDENTIAL_ID,
                            'secret' => OS_CREDENTIAL_SECRET
                        ]
                    ]
                ]
            ]
        ]);

        $token = $this->model(Token::class)->populateFromResponse($resp);
        return $token;
    }
}


global $os;

$os = new \OpenStack\OpenStack([
    'authUrl' => OS_AUTH_URL,
    'region' => OS_REGION,
    'identityService' => AppCredentialsIdentityService::factory(get_client([
        'authUrl' => OS_AUTH_URL,
        'region' => OS_REGION
    ]))
]);